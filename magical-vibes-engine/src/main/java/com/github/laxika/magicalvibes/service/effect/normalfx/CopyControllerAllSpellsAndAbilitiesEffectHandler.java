package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerAllSpellsAndAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeredAbilityFromSnapshotEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Ulalek's copy-all-spells-and-abilities effect from the current stack. */
@Component
public class CopyControllerAllSpellsAndAbilitiesEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyControllerAllSpellsAndAbilitiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<CardEffect> spellCopyEffects = new ArrayList<>();
        List<CardEffect> abilityCopyEffects = new ArrayList<>();
        for (StackEntry stackEntry : List.copyOf(gameData.stack)) {
            if (stackEntry == entry || !controllerId.equals(stackEntry.getControllerId())) {
                continue;
            }

            StackEntry snapshot = new StackEntry(stackEntry);
            switch (stackEntry.getEntryType()) {
                case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL, PLANESWALKER_SPELL,
                        BATTLE_SPELL, SORCERY_SPELL, INSTANT_SPELL ->
                        spellCopyEffects.add(new CopyControllerCastSpellEffect(snapshot, controllerId));
                case ACTIVATED_ABILITY ->
                        abilityCopyEffects.add(new CopyControllerActivatedAbilityEffect(snapshot, null, controllerId));
                case TRIGGERED_ABILITY ->
                        abilityCopyEffects.add(new CopyTriggeredAbilityFromSnapshotEffect(snapshot));
            }
        }

        // Spell copies go on the stack first so the copied abilities end up above them, as the card's
        // text orders it ("copy all spells you control, then copy all other activated and triggered abilities").
        List<CardEffect> copyEffects = new ArrayList<>(spellCopyEffects);
        copyEffects.addAll(abilityCopyEffects);
        if (!copyEffects.isEmpty()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, copyEffects);
        }
    }
}
