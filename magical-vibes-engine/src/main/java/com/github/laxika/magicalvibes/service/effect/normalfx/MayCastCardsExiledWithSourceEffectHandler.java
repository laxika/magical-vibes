package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link MayCastCardsExiledWithSourceEffect} (Spell Queller): every card still exiled with
 * the now-departed source permanent is offered to its <em>owner</em> to cast for free.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayCastCardsExiledWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastCardsExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = ((MayCastCardsExiledWithSourceEffect) effect).sourcePermanentId();
        if (sourcePermanentId == null) return;

        List<ExiledCardEntry> exiled;
        synchronized (gameData.exiledCards) {
            exiled = new ArrayList<>(gameData.exiledCards);
        }

        for (ExiledCardEntry exiledEntry : exiled) {
            if (!sourcePermanentId.equals(exiledEntry.sourcePermanentId())) continue;

            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    exiledEntry.card(),
                    exiledEntry.ownerId(),
                    List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                    "Cast " + exiledEntry.card().getName() + " without paying its mana cost?",
                    exiledEntry.card().getId()
            ));
            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(),
                    " left the battlefield — its owner may cast ", exiledEntry.card(), " for free."));
            log.info("Game {} - {} offers a free cast of exiled {}", gameData.id,
                    entry.getCard().getName(), exiledEntry.card().getName());
        }
    }
}
