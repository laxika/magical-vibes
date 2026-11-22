package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Exiles the target spells from the stack. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetSpellEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final CounterSupport counterSupport;
    private final GameLogService gameLogService;
    private final StateTriggerService stateTriggerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileTargetSpellEffect) effect;
        entry.recordStep(StepKind.EXILE, StepOutcome.NONE);
        List<UUID> targetIds = List.copyOf(entry.targetsForEffect(effect));
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        UUID sourceId = exile.trackWithSource() ? sourcePermanentId(gameData, entry) : null;
        for (UUID targetId : targetIds) {
            StackEntry spell = gameData.stack.stream()
                    .filter(stacked -> stacked.getTargetableId().equals(targetId))
                    .findFirst()
                    .orElse(null);
            if (spell == null) {
                continue;
            }
            gameData.stack.remove(spell);
            stateTriggerService.cleanupResolvedStateTrigger(gameData, spell);
            if (spell.isCopy()) {
                gameLogService.append(gameData, GameLog.cardThen(spell.getCard(), " (a copy) ceases to exist."));
                continue;
            }
            if (counterSupport.replaceFlashforwardDestination(gameData, spell)) {
                gameLogService.append(gameData,
                        GameLog.cardThen(spell.getCard(), " is put on the bottom of its owner's library."));
                continue;
            }
            Card card = spell.getPhysicalCard();
            if (sourceId == null) {
                exileService.exileCard(gameData, spell.getOwnerId(), card);
            } else {
                exileService.exileCard(gameData, spell.getOwnerId(), card, sourceId);
            }
            entry.addToStep(StepKind.EXILE, StepOutcome.ofObjects(List.of(card.getId()), List.of(spell.getOwnerId())));
            gameLogService.append(gameData,
                    GameLog.cardTextCard(spell.getCard(), " is exiled by ", entry.getCard(), "."));
            log.info("Game {} - {} exiled from the stack by {}",
                    gameData.id, spell.getCard().getName(), entry.getCard().getName());
        }
    }

    /**
     * The permanent the ability came from, else matched by its card on its controller's battlefield; none when it
     * already left, and the spell is exiled all the same.
     */
    private static UUID sourcePermanentId(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            return entry.getSourcePermanentId();
        }
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of())) {
            if (permanent.getCard() == entry.getCard()) {
                return permanent.getId();
            }
        }
        return null;
    }
}
