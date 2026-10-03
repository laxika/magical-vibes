package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastArtifactFromHandOrGraveyardByPayingLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MayCastArtifactFromHandOrGraveyardByPayingLifeEqualToManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastArtifactFromHandOrGraveyardByPayingLifeEqualToManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> eligible = new ArrayList<>();

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand != null) {
            eligible.addAll(hand.stream()
                    .filter(card -> !card.isCastOnlyFromGraveyard())
                    .filter(card -> isEligibleArtifact(gameData, controllerId, card))
                    .filter(card -> canPayLife(gameData, controllerId, card))
                    .toList());
        }

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            eligible.addAll(graveyard.stream()
                    .filter(card -> gameQueryService.canCastSpellFromZone(gameData, card, com.github.laxika.magicalvibes.model.Zone.GRAVEYARD, controllerId))
                    .filter(card -> isEligibleArtifact(gameData, controllerId, card))
                    .filter(card -> canPayLife(gameData, controllerId, card))
                    .toList());
        }

        for (int i = eligible.size() - 1; i >= 0; i--) {
            Card card = eligible.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    controllerId,
                    List.of(effect),
                    entry.getCard().getName() + " — Cast " + card.getName()
                            + " by paying life equal to its mana value?"
            ));
        }
    }

    private boolean isEligibleArtifact(GameData gameData, UUID controllerId, Card card) {
        return gameQueryService.cardHasType(card, CardType.ARTIFACT, gameData, controllerId)
                && !gameQueryService.cardHasType(card, CardType.LAND, gameData, controllerId);
    }

    private boolean canPayLife(GameData gameData, UUID playerId, Card card) {
        return gameData.getLife(playerId) >= card.getManaValue()
                && gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameQueryService.canPayLifeForCosts(gameData);
    }
}
