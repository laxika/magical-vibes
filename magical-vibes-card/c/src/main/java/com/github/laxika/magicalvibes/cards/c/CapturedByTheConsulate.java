package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "8")
public class CapturedByTheConsulate extends Card {

    public CapturedByTheConsulate() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect(true, false));
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        null,
                        List.of(new ChangeTargetOfTargetSpellToEnchantedCreatureEffect()),
                        new StackEntryIsSingleTargetPredicate()));
    }
}
