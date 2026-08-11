package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "6")
public class Catastrophe extends Card {

    public Catastrophe() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all lands",
                        new DestroyAllPermanentsEffect(new PermanentIsLandPredicate(), true)),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all creatures",
                        new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate(), true))
        )));
    }
}
