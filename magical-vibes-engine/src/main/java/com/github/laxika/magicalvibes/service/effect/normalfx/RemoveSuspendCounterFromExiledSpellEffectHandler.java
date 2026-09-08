package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSuspendCounterFromExiledSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoveSuspendCounterFromExiledSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveSuspendCounterFromExiledSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = ((RemoveSuspendCounterFromExiledSpellEffect) effect).cardId();
        int index = indexOf(gameData, cardId);
        if (index < 0) return;

        GameData.SuspendedSpellExile pending = gameData.suspendedSpellExiles.get(index);
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled == null) {
            gameData.suspendedSpellExiles.remove(index);
            return;
        }

        int remaining = pending.counters() - 1;
        if (remaining > 0) {
            gameData.suspendedSpellExiles.set(index,
                    new GameData.SuspendedSpellExile(cardId, pending.ownerId(), remaining));
            gameLogService.append(gameData, GameLog.cardThen(exiled.card(),
                    " loses a time counter (" + remaining + " left)."));
            return;
        }

        gameData.suspendedSpellExiles.remove(index);
        boolean creature = exiled.card().hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE);
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                exiled.card(),
                pending.ownerId(),
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect(false, creature)),
                "You may cast " + exiled.card().getName() + " without paying its mana cost.",
                cardId
        ));
        gameLogService.append(gameData, GameLog.cardThen(exiled.card(),
                " loses its last time counter. Its owner may cast it without paying its mana cost."));
    }

    private int indexOf(GameData gameData, UUID cardId) {
        for (int i = 0; i < gameData.suspendedSpellExiles.size(); i++) {
            if (cardId.equals(gameData.suspendedSpellExiles.get(i).cardId())) return i;
        }
        return -1;
    }
}
