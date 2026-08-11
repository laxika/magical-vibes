package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentsSpellsAndAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CounterOpponentsSpellsAndAbilitiesEffectHandler implements NormalEffectHandlerBean {

    private static final Set<StackEntryType> SPELL_TYPES = Set.of(
            StackEntryType.CREATURE_SPELL, StackEntryType.ENCHANTMENT_SPELL,
            StackEntryType.SORCERY_SPELL, StackEntryType.INSTANT_SPELL,
            StackEntryType.ARTIFACT_SPELL, StackEntryType.PLANESWALKER_SPELL);

    private static final Set<StackEntryType> ABILITY_TYPES = Set.of(
            StackEntryType.ACTIVATED_ABILITY, StackEntryType.TRIGGERED_ABILITY);

    private final CounterSupport counterSupport;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterOpponentsSpellsAndAbilitiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterOpponentsSpellsAndAbilitiesEffect counterEffect =
                (CounterOpponentsSpellsAndAbilitiesEffect) effect;
        List<StackEntry> candidates = new ArrayList<>(gameData.stack);
        int counteredCount = 0;

        for (StackEntry candidate : candidates) {
            if (!SPELL_TYPES.contains(candidate.getEntryType())
                    && !ABILITY_TYPES.contains(candidate.getEntryType())) {
                continue;
            }
            if (entry.getControllerId().equals(candidate.getControllerId())
                    || !gameData.stack.contains(candidate)) {
                continue;
            }

            StackEntry target = counterSupport.findCounterTarget(
                    gameData, candidate.getCard().getId(), entry);
            if (target != null && counterSupport.counterSpell(gameData, entry, target)) {
                counteredCount++;
            }
        }

        if (counteredCount > 0) {
            CreateTokenEffect tokens = counterEffect.tokenTemplate().withAmount(counteredCount);
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData, entry.getControllerId(), tokens, entry.getCard().getSetCode()));
        }

        log.info("Game {} - {} countered {} opponent spells or abilities",
                gameData.id, entry.getCard().getName(), counteredCount);
    }
}
