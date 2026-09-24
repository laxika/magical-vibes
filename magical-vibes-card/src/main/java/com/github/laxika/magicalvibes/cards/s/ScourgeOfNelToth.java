package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "C15", collectorNumber = "21")
public class ScourgeOfNelToth extends Card {

    public ScourgeOfNelToth() {
        addCastingOption(new GraveyardCast(null, "{B}{B}", List.of(
                new SacrificePermanentsCost(2, new PermanentIsCreaturePredicate()))));
    }
}
