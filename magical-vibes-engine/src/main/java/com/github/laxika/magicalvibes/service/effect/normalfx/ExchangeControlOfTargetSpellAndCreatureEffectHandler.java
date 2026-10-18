package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetSpellAndCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeControlOfTargetSpellAndCreatureEffectHandler implements NormalEffectHandlerBean {

    private static final Set<StackEntryType> NONCREATURE_SPELL_TYPES = Set.of(
            StackEntryType.ENCHANTMENT_SPELL,
            StackEntryType.SORCERY_SPELL,
            StackEntryType.INSTANT_SPELL,
            StackEntryType.ARTIFACT_SPELL,
            StackEntryType.PLANESWALKER_SPELL,
            StackEntryType.BATTLE_SPELL);

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeControlOfTargetSpellAndCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExchangeControlOfTargetSpellAndCreatureEffect exchange =
                (ExchangeControlOfTargetSpellAndCreatureEffect) effect;
        int creatureTargetGroup = entry.getTargetingCard().getEffectTargetIndex(exchange);
        if (creatureTargetGroup <= 0) {
            return;
        }

        List<UUID> spellTargets = entry.targetsForGroup(creatureTargetGroup - 1);
        List<UUID> creatureTargets = entry.targetsForGroup(creatureTargetGroup);
        if (spellTargets.isEmpty()) {
            return;
        }

        UUID spellId = spellTargets.getFirst();
        StackEntry spellEntry = gameData.stack.stream()
                .filter(stackEntry -> spellId.equals(stackEntry.getTargetableId()))
                .findFirst()
                .orElse(null);
        if (spellEntry == null || !NONCREATURE_SPELL_TYPES.contains(spellEntry.getEntryType())) {
            return;
        }

        Permanent creature = creatureTargets.isEmpty()
                ? null : gameQueryService.findPermanentById(gameData, creatureTargets.getFirst());
        if (creature != null && gameQueryService.isCreature(gameData, creature)) {
            exchangeControl(gameData, entry, spellEntry, creature);
        }

        // The spell's controller chooses new targets even when the exchange did not happen (e.g. the
        // creature left the battlefield or both were already controlled by the same player).
        if (hasSpellTargets(spellEntry)) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    spellEntry.getControllerId(),
                    List.of(new ChooseNewTargetsForTargetSpellEffect()),
                    "Choose new targets for " + spellEntry.getCard().getName() + "?",
                    spellEntry.getCard().getId()));
        }
    }

    private void exchangeControl(GameData gameData, StackEntry entry, StackEntry spellEntry, Permanent creature) {
        UUID spellControllerId = spellEntry.getControllerId();
        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (spellControllerId == null || creatureControllerId == null
                || spellControllerId.equals(creatureControllerId)) {
            return;
        }

        boolean creatureControlApplied = creatureControlService.applyControlEffect(
                gameData,
                spellControllerId,
                creature,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                ControlDuration.PERMANENT.toEffectDuration(),
                null,
                entry.getCard().getName());
        if (!creatureControlApplied) {
            return;
        }

        UUID spellOwnerId = spellEntry.getOwnerId();
        spellEntry.setOwnerIdOverride(spellOwnerId);
        spellEntry.gainControl(creatureControllerId);

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" exchanges control of ")
                .card(spellEntry.getCard())
                .text(" and ")
                .card(creature.getCard())
                .text(".")
                .build());
        log.info("Game {} - {} exchanges control of {} and {}", gameData.id,
                entry.getCard().getName(), spellEntry.getCard().getName(), creature.getCard().getName());
    }

    private boolean hasSpellTargets(StackEntry spellEntry) {
        return spellEntry.getTargetId() != null
                || !spellEntry.getDeclaredTargetIds().isEmpty()
                || !spellEntry.getTargetCardIds().isEmpty();
    }
}
