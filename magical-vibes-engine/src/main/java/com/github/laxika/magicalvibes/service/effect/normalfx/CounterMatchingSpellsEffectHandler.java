package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterMatchingSpellsEffect;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Counters every spell on the stack matching the effect's {@link StackEntryPredicate} (Counterflux
 * overload). Snapshot-then-counter because removing mutates the stack. Abilities are skipped.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CounterMatchingSpellsEffectHandler implements NormalEffectHandlerBean {

    private static final Set<StackEntryType> SPELL_TYPES = Set.of(
            StackEntryType.CREATURE_SPELL, StackEntryType.ENCHANTMENT_SPELL,
            StackEntryType.SORCERY_SPELL, StackEntryType.INSTANT_SPELL,
            StackEntryType.ARTIFACT_SPELL, StackEntryType.PLANESWALKER_SPELL,
            StackEntryType.BATTLE_SPELL);

    private final CounterSupport counterSupport;
    private final TargetLegalityService targetLegalityService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterMatchingSpellsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterMatchingSpellsEffect matching = (CounterMatchingSpellsEffect) effect;

        List<StackEntry> toCounter = new ArrayList<>();
        for (StackEntry se : gameData.stack) {
            if (!SPELL_TYPES.contains(se.getEntryType())) {
                continue;
            }
            if (!targetLegalityService.matchesStackEntryPredicate(
                    gameData, se, matching.filter(), entry.getControllerId())) {
                continue;
            }
            toCounter.add(se);
        }

        int counteredCount = 0;
        for (StackEntry target : toCounter) {
            StackEntry resolved = counterSupport.findCounterTarget(gameData, target.getCard().getId(), entry);
            if (resolved != null && counterSupport.counterSpell(gameData, entry, resolved)) {
                counteredCount++;
            }
        }
        entry.setEventValue(counteredCount);
    }
}
