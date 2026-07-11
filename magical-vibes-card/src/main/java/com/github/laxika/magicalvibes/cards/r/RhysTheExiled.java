package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "135")
public class RhysTheExiled extends Card {

    public RhysTheExiled() {
        // Whenever Rhys the Exiled attacks, you gain 1 life for each Elf you control.
        addEffect(EffectSlot.ON_ATTACK, new GainLifeEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ELF), CountScope.CONTROLLER)));

        // {B}, Sacrifice an Elf: Regenerate Rhys the Exiled.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new SacrificePermanentCost(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.ELF)
                        )),
                        "Sacrifice an Elf",
                        false
                ), new RegenerateEffect()),
                "{B}, Sacrifice an Elf: Regenerate Rhys the Exiled."
        ));
    }
}
