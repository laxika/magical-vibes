package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "8")
public class CapturedByTheConsulate extends Card {

    public CapturedByTheConsulate() {
        target(TargetFilters.creatureAnOpponentControls())
                .addStaticEffect(new EnchantedCreatureCantAttackOrBlockEffect(true, false));
        addTrigger(Triggers.opponentCastsSpell(),
                new SpellCastTriggerEffect(
                        null,
                        List.of(new ChangeTargetOfTargetSpellToEnchantedCreatureEffect()),
                        new StackEntryIsSingleTargetPredicate()));
    }
}
