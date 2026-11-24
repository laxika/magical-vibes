package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfPermanentAndSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link ExchangeControlOfPermanentAndSpellEffect}: all of the exchange or none of it (CR 701.12a), nothing
 * when one player controls both. The permanent's control effect is applied first, as the half that can be refused
 * ("can't be controlled by other players"); the spell changes controller only once it has been applied.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeControlOfPermanentAndSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeControlOfPermanentAndSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExchangeControlOfPermanentAndSpellEffect exchange = (ExchangeControlOfPermanentAndSpellEffect) effect;
        entry.recordStep(StepKind.GAIN_CONTROL, StepOutcome.NONE);

        StackEntry spell = subjectResolutionService.spellOnStack(gameData, entry, exchange.spell());
        List<UUID> permanentIds = subjectResolutionService.permanentIds(gameData, entry, effect, exchange.permanent());
        Permanent permanent = permanentIds.isEmpty() ? null
                : gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
        if (spell == null || permanent == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s exchange has no effect."));
            return;
        }

        UUID permanentControllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        UUID spellControllerId = spell.getControllerId();
        if (permanentControllerId == null || spellControllerId == null
                || permanentControllerId.equals(spellControllerId)) {
            return;
        }

        if (!creatureControlService.applyControlEffect(gameData, spellControllerId, permanent,
                new GainControlOfTargetEffect(Duration.PERMANENT), Duration.PERMANENT, null,
                entry.getCard().getName())) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s exchange has no effect."));
            return;
        }
        spell.setOwnerIdOverride(spell.getOwnerId());
        spell.gainControl(permanentControllerId);
        entry.recordStep(StepKind.GAIN_CONTROL,
                StepOutcome.ofObjects(List.of(permanent.getId()), List.of(spellControllerId)));

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" exchanges control of ")
                .card(spell.getCard())
                .text(" and ")
                .card(permanent.getCard())
                .text(".")
                .build());
        log.info("Game {} - {} exchanges control of {} and {}", gameData.id,
                entry.getCard().getName(), spell.getCard().getName(), permanent.getCard().getName());
    }
}
