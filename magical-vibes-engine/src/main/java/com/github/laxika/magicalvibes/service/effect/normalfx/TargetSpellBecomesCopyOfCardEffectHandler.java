package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellBecomesCopyOfCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetSpellBecomesCopyOfCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetSpellBecomesCopyOfCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetSpellBecomesCopyOfCardEffect copyEffect = (TargetSpellBecomesCopyOfCardEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null || copyEffect.copyOfCard() == null) {
            return;
        }

        StackEntry targetSpell = gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getTargetableId().equals(targetId))
                .findFirst()
                .orElse(null);
        if (targetSpell == null) {
            log.info("Game {} - Target spell no longer on stack for copy-characteristics effect", gameData.id);
            return;
        }

        Card originalCard = targetSpell.getCard();
        Card copiedCard = originalCard.createRuntimeCopyWithFace(copyEffect.copyOfCard());
        // CR 702.34a: the exile-instead replacement is part of the flashback ability, which the
        // spell no longer has once it has become Lightning Bolt.
        targetSpell.setCastWithFlashback(false);
        targetSpell.setCastCard(copiedCard);
        targetSpell.setDescription(copiedCard.getName());
        targetSpell.replaceEffectsToResolve(copiedCard.getCastingCostsAndSpellEffects());
        targetSpell.setTargetFilter(copiedCard.getTargetFilter());
        targetSpell.setTargetFilters(List.of());
        targetSpell.setTargetZone(null);
        targetSpell.setTargetCardIds(List.of());
        targetSpell.setTargetCardGroupSizes(List.of());
        targetSpell.setPrimaryTargetStoredSeparately(false);

        List<UUID> declaredTargets = targetSpell.getDeclaredTargetIds();
        if (targetSpell.getTargetId() == null && !declaredTargets.isEmpty()) {
            targetSpell.setTargetId(declaredTargets.getFirst());
        }
        targetSpell.setDeclaredTargetIds(List.of());
        if (copiedCard.hasType(CardType.INSTANT)) {
            targetSpell.setEntryType(StackEntryType.INSTANT_SPELL);
        } else if (copiedCard.hasType(CardType.SORCERY)) {
            targetSpell.setEntryType(StackEntryType.SORCERY_SPELL);
        }

        gameLogService.append(gameData,
                GameLog.textCardText(originalCard.getName() + " becomes a copy of ", copiedCard, "."));
        log.info("Game {} - {} becomes a copy of {}", gameData.id, originalCard.getName(), copiedCard.getName());

        // CR 115.7d: "choose new targets" lets the player leave the target unchanged or choose any legal
        // target, including the current one, so reuse the copy-retarget flow rather than the
        // "change the target" flow that excludes the current target (CR 115.7a).
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                targetSpell.getControllerId(),
                List.of(CopySpellEffect.times(1)),
                "Choose new targets for " + copiedCard.getName() + "?",
                targetSpell.getTargetableId()));
    }
}
