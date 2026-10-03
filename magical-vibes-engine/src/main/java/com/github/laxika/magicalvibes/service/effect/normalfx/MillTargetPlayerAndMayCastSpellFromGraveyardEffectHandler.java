package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastSpellFromTargetGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerAndMayCastSpellFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Sorcerous Squall's mill-then-free-cast effect. */
@Component
@RequiredArgsConstructor
public class MillTargetPlayerAndMayCastSpellFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillTargetPlayerAndMayCastSpellFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillTargetPlayerAndMayCastSpellFromGraveyardEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerDecks.containsKey(targetPlayerId)) {
            return;
        }

        graveyardService.resolveMillPlayer(gameData, targetPlayerId, e.count());

        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        if (graveyard == null) {
            return;
        }

        List<Card> castable = graveyard.stream()
                .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                .toList();
        for (int i = castable.size() - 1; i >= 0; i--) {
            Card card = castable.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    entry.getControllerId(),
                    List.of(new MayCastSpellFromTargetGraveyardWithoutPayingManaCostEffect()),
                    "Cast " + card.getName() + " without paying its mana cost?"
            ));
        }
    }
}
