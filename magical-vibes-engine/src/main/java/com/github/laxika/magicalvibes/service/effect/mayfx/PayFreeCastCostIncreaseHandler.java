package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayFreeCastCostIncreaseEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CastCardWithoutPayingManaCostIfAbleEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastSupport;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Answers the cost-increase payment question of a free cast ({@link PayFreeCastCostIncreaseEffect}). The caster has
 * activated the mana abilities they chose while it was asked (CR 601.2g); accepted with enough mana in the pool, the
 * paused flow goes on and pays from it, otherwise the card is not cast.
 */
@Component
public class PayFreeCastCostIncreaseHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    @Autowired
    @Lazy
    private SpellCastingService spellCastingService;
    @Autowired
    @Lazy
    private ExileFreeCastSupport exileFreeCastSupport;
    @Autowired
    @Lazy
    private ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    @Autowired
    @Lazy
    private CastCardWithoutPayingManaCostIfAbleEffectHandler castCardWithoutPayingManaCostIfAbleEffectHandler;

    public PayFreeCastCostIncreaseHandler(GameLogService gameLogService) {
        this.gameLogService = gameLogService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayFreeCastCostIncreaseEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        PayFreeCastCostIncreaseEffect payment = ability.effects().stream()
                .filter(PayFreeCastCostIncreaseEffect.class::isInstance)
                .map(PayFreeCastCostIncreaseEffect.class::cast)
                .findFirst()
                .orElseThrow();
        boolean paid = accepted && spellCastingService.canPayFreeCastCostIncreases(
                gameData, player.getId(), ability.sourceCard(), payment.zone());
        if (!paid) {
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername()
                    + " doesn't pay the cost increase of ", ability.sourceCard(), "."));
        }
        switch (payment.resume()) {
            case EXILE_CAST -> exileFreeCastSupport.resumeAfterCostIncreaseDecision(
                    gameData, player, payment.cardId(), payment.exileCast(), paid);
            case FREE_CAST_QUEUE -> exileFreeCastQueueSupport.resumeAfterCostIncreaseDecision(
                    gameData, player.getId(), payment.cardId(), paid);
            case HAND_CAST -> castCardWithoutPayingManaCostIfAbleEffectHandler.resumeAfterCostIncreaseDecision(
                    gameData, player, ability.sourceCard(), paid);
        }
    }
}
