package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Play an exiled card without paying its mana cost — Guile's counter replacement, Spell Queller's
 * leaves-the-battlefield trigger. Declining leaves the card exiled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayPlayExiledCardWithoutPayingManaCostHandler implements MayEffectHandlerBean {

    private final ExileFreeCastSupport exileFreeCastSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPlayExiledCardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted && ability.targetCardId() != null) {
            if (isExclusive(ability)) {
                // "Cast a spell from among those cards" — one offer per exiled card was queued, so
                // withdraw the siblings; only a single spell may be cast (Shell of the Last Kappa).
                gameData.pendingMayAbilities.removeIf(pending -> pending != ability && isExclusive(pending));
            }
            exileFreeCastSupport.castFromExileWithoutPaying(gameData, player, ability.targetCardId());
        } else {
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " declines to play " , ability.sourceCard(), "."));
            log.info("Game {} - {} declines to play exiled {}", gameData.id,
                    player.getUsername(), ability.sourceCard().getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    private boolean isExclusive(PendingMayAbility ability) {
        return ability.effects().stream()
                .anyMatch(effect -> effect instanceof MayPlayExiledCardWithoutPayingManaCostEffect e && e.exclusive());
    }
}
