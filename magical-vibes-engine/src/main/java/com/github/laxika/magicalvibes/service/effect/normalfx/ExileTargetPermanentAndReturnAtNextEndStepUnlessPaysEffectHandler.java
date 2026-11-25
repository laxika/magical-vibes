package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnAtNextEndStepUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.service.effect.DelayedExiledCardReturns;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentAndReturnAtNextEndStepUnlessPaysEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentAndReturnAtNextEndStepUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        } else if (targetIds.isEmpty() && entry.getCard().getTargetGroups().size() == 1) {
            targetIds = entry.getTargetIds();
        }

        boolean queued = false;
        for (UUID targetId : targetIds) {
            if (targetId == null) {
                continue;
            }
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            List<Card> cards = target.cardsLeavingBattlefield();
            if (cards.isEmpty() || !permanentRemovalService.removePermanentToExile(gameData, target)) {
                continue;
            }

            Card exiledCard = cards.getFirst();
            queued = true;
            gameData.temporaryGlobalTriggeredAbilities.add(returnUnlessPaid(gameData,
                    exiledCard.getId(), entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId()));
            gameLogService.append(gameData, GameLog.cardThen(exiledCard,
                    " is exiled. It will return at the beginning of the next end step unless its controller pays {3}{B}."));
            log.info("Game {} - {} exiles {}; return delayed until next end step unless paid",
                    gameData.id, entry.getCard().getName(), exiledCard.getName());
        }

        if (!queued && targetIds.stream().noneMatch(java.util.Objects::nonNull)) {
            // "Up to one" with no target chosen: the delayed trigger is still created and its payment
            // still offered, but there is no card to return.
            gameData.temporaryGlobalTriggeredAbilities.add(returnUnlessPaid(gameData,
                    null, entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId()));
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * The delayed triggered ability "at the beginning of the next end step, you may pay {3}{B}. If you don't, return
     * that card to the battlefield under its owner's control" of a spell or ability of {@code sourceCard} controlled by
     * {@code controllerId} that exiled {@code cardId} (none when it exiled no card).
     */
    private static TemporaryGlobalTriggeredAbility returnUnlessPaid(GameData gameData, UUID cardId,
                                                                    UUID controllerId, Card sourceCard,
                                                                    UUID sourcePermanentId) {
        UnlessPaysEffect payOrReturn = UnlessPaysEffect.unlessYouPay("{3}{B}",
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.EXILE)))
                .withPrompt("Pay {3}{B}? If you don't, that card returns to the battlefield under its owner's control.");
        return TemporaryGlobalTriggeredAbility.atNextStep(controllerId, sourceCard,
                new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY), payOrReturn), sourcePermanentId, null, null,
                DelayedExiledCardReturns.exiled(gameData, java.util.Collections.singletonList(cardId)));
    }
}
