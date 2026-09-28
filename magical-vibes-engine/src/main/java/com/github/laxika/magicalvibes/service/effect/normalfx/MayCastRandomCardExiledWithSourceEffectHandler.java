package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastRandomCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

/** Offers one random nonland card exiled with the source for a free cast. */
@Component
public class MayCastRandomCardExiledWithSourceEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastRandomCardExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return;

        List<Card> exiled = gameData.getCardsExiledByPermanent(sourcePermanentId).stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .toList();
        if (exiled.size() < 4) return;

        Card selected = exiled.get(ThreadLocalRandom.current().nextInt(exiled.size()));
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                "Cast " + selected.getName() + " without paying its mana cost?",
                selected.getId(),
                null,
                sourcePermanentId));
    }
}
