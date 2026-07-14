package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardDrawingEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellingEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureBoostEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDealingEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.LifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.RemovalEffect;

/**
 * Classifies instant-speed cards by their primary role. For instants, scans the
 * card's SPELL effects. For flash creatures, scans ON_ENTER_BATTLEFIELD effects.
 * Returns the first matching category. Priority order ensures
 * counter > removal > combat trick > burn > card advantage > other/flash_creature.
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
        if (effect instanceof CounterSpellingEffect) {
            return InstantCategory.COUNTERSPELL;
        }

        // Hard removal (destroy, exile, single-target bounce). removalKind() is non-null exactly for
        // the single-target destroy/exile/bounce configurations.
        if (effect instanceof RemovalEffect rem && rem.removalKind() != null) return InstantCategory.REMOVAL;

        // Damage-based removal (anything that can damage creatures — target-creature or any-target).
        // Player-only damage (canDamageCreatures() == false) drops through to the burn check below.
        if (effect instanceof DamageDealingEffect dmg && dmg.canDamageCreatures()) return InstantCategory.REMOVAL;
        // Creature-or-planeswalker fixed damage does not implement DamageDealingEffect (that would
        // newly score it in SpellEvaluator); handled explicitly here.
        if (effect instanceof DealDamageToTargetCreatureOrPlaneswalkerEffect) return InstantCategory.REMOVAL;

        // Combat tricks (targeted pump spells)
        if (effect instanceof CreatureBoostEffect) return InstantCategory.COMBAT_TRICK;

        // Burn to face
        if (effect instanceof DealDamageToPlayersEffect dmg && dmg.recipient() == DamageRecipient.TARGET_PLAYER) return InstantCategory.BURN_TO_FACE;

        // Card advantage
        if (effect instanceof CardDrawingEffect) return InstantCategory.CARD_ADVANTAGE;
        if (effect instanceof LifeGainEffect) return InstantCategory.CARD_ADVANTAGE;

        return null;
    }

    /**
     * Classifies a flash creature by scanning its ON_ENTER_BATTLEFIELD effects.
     * If the creature has a notable ETB (removal, card draw, etc.), it returns
     * the corresponding instant category so timing rules match. Otherwise returns
     * FLASH_CREATURE for generic "cast at end of opponent's turn" handling.
     */
    public static InstantCategory classifyFlashCreature(Card card) {
        if (!card.hasType(CardType.CREATURE) || !card.getKeywords().contains(Keyword.FLASH)) {
            return InstantCategory.OTHER;
        }

        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            InstantCategory cat = classifySingleEffect(effect);
            if (cat != null) return cat;
        }

        return InstantCategory.FLASH_CREATURE;
    }
}
