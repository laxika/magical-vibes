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
import com.github.laxika.magicalvibes.service.effect.normalfx.SplitCardCastSupport;
import com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
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
    @Autowired
    @Lazy
    private LibraryChoiceHandlerService libraryChoiceHandlerService;
    @Autowired
    @Lazy
    private MayCastHandlerService mayCastHandlerService;
    @Autowired
    @Lazy
    private SplitCardCastSupport splitCardCastSupport;

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
        switch (payment.paused()) {
            case PayFreeCastCostIncreaseEffect.ExileCast exile -> exileFreeCastSupport.resumeAfterCostIncreaseDecision(
                    gameData, player, payment.cardId(), exile.offer(), paid);
            case PayFreeCastCostIncreaseEffect.QueueCast ignored -> exileFreeCastQueueSupport
                    .resumeAfterCostIncreaseDecision(gameData, player.getId(), payment.cardId(), paid);
            case PayFreeCastCostIncreaseEffect.HandCast ignored -> castCardWithoutPayingManaCostIfAbleEffectHandler
                    .resumeAfterCostIncreaseDecision(gameData, player, ability.sourceCard(), paid);
            case PayFreeCastCostIncreaseEffect.LibraryCast library -> libraryChoiceHandlerService
                    .resumeAfterCostIncreaseDecision(gameData, player, ability.sourceCard(), payment.zone(), library,
                            paid);
            case PayFreeCastCostIncreaseEffect.RevealedCast revealed -> mayCastHandlerService
                    .resumeRevealedCastAfterCostIncreaseDecision(gameData, player, ability.sourceCard(), revealed,
                            paid);
            case PayFreeCastCostIncreaseEffect.LibraryTopCast top -> mayCastHandlerService
                    .resumeLibraryTopCastAfterCostIncreaseDecision(gameData, player, top, paid);
            case PayFreeCastCostIncreaseEffect.SplitCast split -> splitCardCastSupport
                    .resumeAfterCostIncreaseDecision(gameData, split.targeting(), paid);
        }
    }
}
