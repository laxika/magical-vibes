package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromSideboardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromTargetPlayerSideboardWithoutPayingManaCostEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Command the Chaff's targeted sideboard free-cast instruction. */
@Component
public class MayCastFromTargetPlayerSideboardWithoutPayingManaCostEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastFromTargetPlayerSideboardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sideboardOwnerId = entry.getTargetId();
        if (sideboardOwnerId == null) {
            return;
        }

        List<Card> sideboard = gameData.playerSideboards.getOrDefault(sideboardOwnerId, List.of());
        List<Card> castableCards = sideboard.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .toList();
        for (int i = castableCards.size() - 1; i >= 0; i--) {
            Card card = castableCards.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    entry.getControllerId(),
                    List.of(new MayCastFromSideboardWithoutPayingManaCostEffect(sideboardOwnerId)),
                    "Cast " + card.getName() + " without paying its mana cost?"));
        }
    }
}
