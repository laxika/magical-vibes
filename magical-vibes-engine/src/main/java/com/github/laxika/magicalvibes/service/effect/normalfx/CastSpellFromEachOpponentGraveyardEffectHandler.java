package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastSpellFromEachOpponentGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardFromOpponentGraveyardWithoutPayingManaCostEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Jetsam's independent one-spell-per-opponent graveyard offers. */
@Component
@RequiredArgsConstructor
public class CastSpellFromEachOpponentGraveyardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastSpellFromEachOpponentGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        for (int opponentIndex = gameData.orderedPlayerIds.size() - 1; opponentIndex >= 0; opponentIndex--) {
            UUID opponentId = gameData.orderedPlayerIds.get(opponentIndex);
            if (opponentId.equals(controllerId)) {
                continue;
            }

            List<Card> graveyard = gameData.playerGraveyards.get(opponentId);
            if (graveyard == null) {
                continue;
            }

            for (int cardIndex = graveyard.size() - 1; cardIndex >= 0; cardIndex--) {
                Card card = graveyard.get(cardIndex);
                if (card.hasType(CardType.LAND)) {
                    continue;
                }

                String opponentName = gameData.playerIdToName.get(opponentId);
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        card,
                        controllerId,
                        List.of(new MayCastCardFromOpponentGraveyardWithoutPayingManaCostEffect(opponentId)),
                        entry.getCard().getName() + " — Cast " + card.getName()
                                + " from " + opponentName + "'s graveyard without paying its mana cost?"));
            }
        }
    }
}
