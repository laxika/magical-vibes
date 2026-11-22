package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndReturnAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.effect.DelayedExiledCardReturns;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureAndReturnAtNextEndStepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureAndReturnAtNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        } else if (targetIds.isEmpty() && entry.getCard().getTargetGroups().size() == 1) {
            targetIds = entry.getTargetIds();
        }

        for (UUID targetId : targetIds) {
            if (targetId == null) {
                continue;
            }
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            List<Card> exiledCards = List.copyOf(target.cardsLeavingBattlefield());
            if (exiledCards.isEmpty() || !permanentRemovalService.removePermanentToExile(gameData, target)) {
                continue;
            }

            for (Card exiledCard : exiledCards) {
                ExiledCardEntry exiled = gameData.findExiledCard(exiledCard.getId());
                if (exiled == null) {
                    continue;
                }
                gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                        entry.getControllerId(), entry.getCard(),
                        new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY),
                                new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.EXILE))),
                        entry.getSourcePermanentId(), null, null,
                        DelayedExiledCardReturns.exiled(gameData, List.of(exiledCard.getId()))));
                gameLogService.append(gameData, GameLog.cardThen(exiledCard,
                        " is exiled. It will return at the beginning of the next end step under its owner's control."));
                log.info("Game {} - {} exiles {}; return delayed until next end step under its owner's control",
                        gameData.id, entry.getCard().getName(), exiledCard.getName());
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
