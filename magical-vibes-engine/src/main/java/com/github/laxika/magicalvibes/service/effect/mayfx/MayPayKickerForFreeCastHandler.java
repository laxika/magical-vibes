package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.FreeCastKickerOffer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayKickerForFreeCastEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Pays (or skips) the kicker cost of a spell being cast from exile without paying its mana cost, then resumes the cast. */
@Component
@RequiredArgsConstructor
public class MayPayKickerForFreeCastHandler implements MayEffectHandlerBean {

    private final ExileFreeCastSupport exileFreeCastSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayKickerForFreeCastEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID cardId = ability.targetCardId();
        FreeCastKickerOffer offer = cardId == null ? null : gameData.pendingFreeCastKickerOffers.remove(cardId);
        ExiledCardEntry exiled = cardId == null ? null : gameData.findExiledCard(cardId);
        if (offer == null || exiled == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.freeCastKickerDecidedIds.add(cardId);
        if (accepted) {
            KickerEffect kicker = exiled.card().getEffects(EffectSlot.STATIC).stream()
                    .filter(KickerEffect.class::isInstance)
                    .map(KickerEffect.class::cast)
                    .findFirst()
                    .orElse(null);
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            if (kicker != null && kicker.hasManaCost() && pool != null) {
                ManaCost cost = new ManaCost(kicker.cost());
                if (cost.canPay(pool, 0)) {
                    cost.pay(pool, 0);
                    gameData.freeCastKickedIds.add(cardId);
                    gameLogService.append(gameData, GameLog.text(player.getUsername()
                            + " pays the kicker cost " + kicker.cost() + "."));
                }
            }
        }
        exileFreeCastSupport.resumeAfterKickerDecision(gameData, player, cardId, offer);
    }
}
