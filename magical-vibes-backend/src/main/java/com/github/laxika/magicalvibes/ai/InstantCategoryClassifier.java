package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandWithManaValueConditionalEffect;

/**
 * Classifies instant cards by their primary role. Scans the card's SPELL
 * effects and returns the first matching category. Priority order ensures
 * counter > removal > combat trick > burn > card advantage > other.
 */
public final class InstantCategoryClassifier {

    private InstantCategoryClassifier() {}

    public static InstantCategory classify(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            // Modal spells: classify each option's effect and return the highest-priority
            // usable category. Counter modes are skipped because the AI can't target spells.
            if (effect instanceof ChooseOneEffect coe) {
                boolean hasRemoval = false, hasCombatTrick = false;
                boolean hasBurnToFace = false, hasCardAdvantage = false;
                for (ChooseOneEffect.ChooseOneOption option : coe.options()) {
                    InstantCategory cat = classifySingleEffect(option.effect());
                    if (cat == null) continue;
                    switch (cat) {
                        case REMOVAL -> hasRemoval = true;
                        case COMBAT_TRICK -> hasCombatTrick = true;
                        case BURN_TO_FACE -> hasBurnToFace = true;
                        case CARD_ADVANTAGE -> hasCardAdvantage = true;
                        default -> {} // COUNTERSPELL and OTHER skipped
                    }
                }
                if (hasRemoval) return InstantCategory.REMOVAL;
                if (hasCombatTrick) return InstantCategory.COMBAT_TRICK;
                if (hasBurnToFace) return InstantCategory.BURN_TO_FACE;
                if (hasCardAdvantage) return InstantCategory.CARD_ADVANTAGE;
                return InstantCategory.OTHER;
            }

            InstantCategory cat = classifySingleEffect(effect);
            if (cat != null) return cat;
        }
        return InstantCategory.OTHER;
    }

    private static InstantCategory classifySingleEffect(CardEffect effect) {
        // Counter spells (highest priority — reactive by nature)
        if (effect instanceof CounterSpellEffect
                || effect instanceof CounterSpellAndExileEffect
                || effect instanceof CounterUnlessPaysEffect) {
            return InstantCategory.COUNTERSPELL;
        }

        // Hard removal (destroy, exile, bounce)
        if (effect instanceof DestroyTargetPermanentEffect) return InstantCategory.REMOVAL;
        if (effect instanceof ExileTargetPermanentEffect) return InstantCategory.REMOVAL;
        if (effect instanceof ReturnTargetPermanentToHandEffect) return InstantCategory.REMOVAL;
        if (effect instanceof ReturnTargetPermanentToHandWithManaValueConditionalEffect) return InstantCategory.REMOVAL;

        // Damage-based removal (targets creatures)
        if (effect instanceof DealDamageToTargetCreatureEffect) return InstantCategory.REMOVAL;
        if (effect instanceof DealDamageToTargetCreatureOrPlaneswalkerEffect) return InstantCategory.REMOVAL;
        if (effect instanceof DealXDamageToTargetCreatureEffect) return InstantCategory.REMOVAL;

        // Damage to any target — primarily removal (can also go face)
        if (effect instanceof DealDamageToAnyTargetEffect) return InstantCategory.REMOVAL;
        if (effect instanceof DealXDamageToAnyTargetEffect) return InstantCategory.REMOVAL;

        // Combat tricks (pump spells)
        if (effect instanceof BoostTargetCreatureEffect) return InstantCategory.COMBAT_TRICK;

        // Burn to face
        if (effect instanceof DealDamageToTargetPlayerEffect) return InstantCategory.BURN_TO_FACE;

        // Card advantage
        if (effect instanceof DrawCardEffect) return InstantCategory.CARD_ADVANTAGE;
        if (effect instanceof GainLifeEffect) return InstantCategory.CARD_ADVANTAGE;

        return null;
    }
}
