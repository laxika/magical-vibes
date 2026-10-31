package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LCC", collectorNumber = "202")
@CardRegistration(set = "C17", collectorNumber = "19")
public class NewBlood extends Card {

    public NewBlood() {
        // As an additional cost to cast this spell, tap an untapped Vampire you control.
        addCastingCost(new TapMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)));

        // Gain control of target creature. Change its text by replacing all instances of one creature type with Vampire.
        target(TargetFilters.creature())
                .addSpellEffect(new GainControlOfTargetEffect(ControlDuration.PERMANENT))
                .addSpellEffect(ChangeColorTextEffect.creatureTypesReplacedWith(CardSubtype.VAMPIRE));
    }
}
