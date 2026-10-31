package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "98")
public class PitilessCarnage extends Card {

    public PitilessCarnage() {
        addSpellEffect(new SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect(
                new PermanentTruePredicate()));

        // Plot {1}{B}{B}
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{B}{B}"))));
    }
}
