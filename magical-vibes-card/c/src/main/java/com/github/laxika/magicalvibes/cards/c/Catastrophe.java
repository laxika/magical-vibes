package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "6")
@CardRegistration(set = "BRB", collectorNumber = "12")
public class Catastrophe extends Card {

    public Catastrophe() {
        // Not modal: lands or creatures is chosen as the spell resolves, and only destroyed
        // creatures are denied regeneration.
        addEffect(EffectSlot.SPELL, new ChooseOneAtResolutionEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all lands",
                        new DestroyAllPermanentsEffect(new PermanentIsLandPredicate(), false)),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all creatures",
                        new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate(), true))
        ))));
    }
}
