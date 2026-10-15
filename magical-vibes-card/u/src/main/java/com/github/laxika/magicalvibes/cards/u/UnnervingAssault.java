package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "EVE", collectorNumber = "114")
public class UnnervingAssault extends Card {

    public UnnervingAssault() {
        // Creatures your opponents control get -1/-0 until end of turn if {U} was spent to cast this
        // spell (opponents' creatures = every creature not controlled by this spell's controller).
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.BLUE),
                new BoostAllCreaturesEffect(-1, 0,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))));

        // Creatures you control get +1/+0 until end of turn if {R} was spent to cast this spell.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.RED),
                new BoostAllOwnCreaturesEffect(1, 0)));
    }
}
