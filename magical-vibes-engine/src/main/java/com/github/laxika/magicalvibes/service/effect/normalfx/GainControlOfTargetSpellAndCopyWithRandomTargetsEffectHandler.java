package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetSpellAndCopyWithRandomTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class GainControlOfTargetSpellAndCopyWithRandomTargetsEffectHandler
        implements NormalEffectHandlerBean {

    private static final Set<StackEntryType> SPELL_TYPES = Set.of(
            StackEntryType.CREATURE_SPELL,
            StackEntryType.ENCHANTMENT_SPELL,
            StackEntryType.SORCERY_SPELL,
            StackEntryType.INSTANT_SPELL,
            StackEntryType.ARTIFACT_SPELL,
            StackEntryType.PLANESWALKER_SPELL,
            StackEntryType.BATTLE_SPELL);

    private final CopySupport copySupport;
    private final GameLogService gameLogService;
    private final TargetLegalityService targetLegalityService;
    private final TargetRedirectionSupport targetRedirectionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfTargetSpellAndCopyWithRandomTargetsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GainControlOfTargetSpellAndCopyWithRandomTargetsEffect chefEffect =
                (GainControlOfTargetSpellAndCopyWithRandomTargetsEffect) effect;
        StackEntry targetSpell = findTargetSpell(gameData, entry.getTargetId(), chefEffect.spellFilter(),
                entry.getControllerId());
        if (targetSpell == null || targetSpell.getCard() == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        UUID ownerId = targetSpell.getOwnerId();
        targetSpell.setOwnerIdOverride(ownerId);
        targetSpell.setControllerId(controllerId);

        Card copyCard = copySupport.createCopyCard(targetSpell.getCard());
        StackEntry copyEntry = copySupport.createCopyStackEntry(
                targetSpell, copyCard, controllerId, targetSpell.getTargetId());
        copyEntry.setTargetFilter(targetSpell.getTargetFilter());
        copyEntry.setTargetFilters(targetSpell.getTargetFilters());
        copyEntry.setTargetGroupSizes(targetSpell.getTargetGroupSizes());
        copyEntry.setPrimaryTargetStoredSeparately(targetSpell.isPrimaryTargetStoredSeparately());
        copyEntry.setNonTargeting(targetSpell.isNonTargeting());
        copySupport.addCopyToStack(gameData, copyEntry);

        reselectTargetsAtRandom(gameData, targetSpell, controllerId);
        reselectTargetsAtRandom(gameData, copyEntry, controllerId);

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" gains control of and copies ")
                .card(targetSpell.getCard())
                .text(".")
                .build());
    }

    private StackEntry findTargetSpell(GameData gameData, UUID targetId, StackEntryPredicate predicate,
                                       UUID controllerId) {
        if (targetId == null) {
            return null;
        }
        return gameData.stack.stream()
                .filter(stackEntry -> targetId.equals(stackEntry.getTargetableId()))
                .filter(stackEntry -> SPELL_TYPES.contains(stackEntry.getEntryType()))
                .filter(stackEntry -> targetLegalityService.matchesStackEntryPredicate(
                        gameData, stackEntry, predicate, controllerId))
                .findFirst()
                .orElse(null);
    }

    private void reselectTargetsAtRandom(GameData gameData, StackEntry targetSpell, UUID excludedControllerId) {
        List<UUID> legalTargets = targetRedirectionSupport.collectValidTargetsIncludingCurrent(gameData, targetSpell)
                .stream()
                .filter(targetId -> !isExcludedTarget(gameData, targetId, excludedControllerId))
                .toList();
        if (legalTargets.isEmpty()) {
            return;
        }

        if (targetSpell.getTargetId() != null) {
            targetSpell.setTargetId(randomTarget(legalTargets));
        }
        List<UUID> declaredTargetIds = targetSpell.getDeclaredTargetIds();
        for (int i = 0; i < declaredTargetIds.size(); i++) {
            targetSpell.replaceTargetIdAt(i, randomTarget(legalTargets));
        }
    }

    private boolean isExcludedTarget(GameData gameData, UUID targetId, UUID excludedControllerId) {
        if (excludedControllerId.equals(targetId)) {
            return true;
        }
        return gameData.playerBattlefields.getOrDefault(excludedControllerId, List.of()).stream()
                .anyMatch(permanent -> permanent.getId().equals(targetId));
    }

    private UUID randomTarget(List<UUID> legalTargets) {
        return legalTargets.get(ThreadLocalRandom.current().nextInt(legalTargets.size()));
    }
}
