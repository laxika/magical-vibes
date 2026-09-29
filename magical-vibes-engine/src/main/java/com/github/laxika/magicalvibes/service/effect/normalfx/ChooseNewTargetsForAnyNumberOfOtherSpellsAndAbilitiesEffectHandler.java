package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForAnyNumberOfOtherSpellsAndAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForStackEntryEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseNewTargetsForAnyNumberOfOtherSpellsAndAbilitiesEffectHandler
        implements NormalEffectHandlerBean {

    private static final Set<StackEntryType> SPELLS_AND_ABILITIES = Set.of(
            StackEntryType.CREATURE_SPELL,
            StackEntryType.ENCHANTMENT_SPELL,
            StackEntryType.SORCERY_SPELL,
            StackEntryType.INSTANT_SPELL,
            StackEntryType.ARTIFACT_SPELL,
            StackEntryType.PLANESWALKER_SPELL,
            StackEntryType.BATTLE_SPELL,
            StackEntryType.ACTIVATED_ABILITY,
            StackEntryType.TRIGGERED_ABILITY);

    private final PsychicBattleSupport psychicBattleSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNewTargetsForAnyNumberOfOtherSpellsAndAbilitiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (int stackIndex = gameData.stack.size() - 1; stackIndex >= 0; stackIndex--) {
            StackEntry targetEntry = gameData.stack.get(stackIndex);
            if (targetEntry == entry
                    || targetEntry.isNonTargeting()
                    || !SPELLS_AND_ABILITIES.contains(targetEntry.getEntryType())) {
                continue;
            }

            List<UUID> targetIds = psychicBattleSupport.targetIds(targetEntry);
            for (int targetIndex = targetIds.size() - 1; targetIndex >= 0; targetIndex--) {
                if (!psychicBattleSupport.collectLegalAlternatives(gameData, targetEntry, targetIndex).isEmpty()) {
                    gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                            entry.getCard(),
                            entry.getControllerId(),
                            List.of(new ChooseNewTargetsForStackEntryEffect(
                                    targetEntry.getTargetableId(), targetIndex)),
                            "Choose new targets for " + targetEntry.getDescription() + "?"));
                }
            }
        }
    }
}
