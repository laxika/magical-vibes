package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfSourceAndTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeControlOfSourceAndTriggeringSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeControlOfSourceAndTriggeringSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID spellId = entry.getTriggeringCardId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (spellId == null || sourcePermanentId == null) {
            return;
        }

        StackEntry spellEntry = gameQueryService.findStackEntryByCardId(gameData, spellId);
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (spellEntry == null || source == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s exchange has no effect."));
            return;
        }

        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, source.getId());
        UUID spellControllerId = spellEntry.getControllerId();
        if (sourceControllerId == null || spellControllerId == null) {
            return;
        }

        UUID spellOwnerId = spellEntry.getOwnerId();
        spellEntry.setOwnerIdOverride(spellOwnerId);
        spellEntry.setControllerId(sourceControllerId);

        creatureControlService.applyControlEffect(
                gameData,
                spellControllerId,
                source,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                EffectDuration.PERMANENT,
                null,
                entry.getCard().getName());

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" exchanges control of itself and ")
                .card(spellEntry.getCard())
                .text(".")
                .build());
        log.info("Game {} - {} exchanges control with {}", gameData.id,
                entry.getCard().getName(), spellEntry.getCard().getName());

        if (hasSpellTargets(spellEntry)) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    entry.getControllerId(),
                    List.of(new ChooseNewTargetsForTargetSpellEffect()),
                    "Choose new targets for " + spellEntry.getCard().getName() + "?",
                    spellEntry.getCard().getId()));
        }
    }

    private boolean hasSpellTargets(StackEntry spellEntry) {
        return spellEntry.getTargetId() != null
                || !spellEntry.getDeclaredTargetIds().isEmpty()
                || !spellEntry.getTargetCardIds().isEmpty();
    }
}
