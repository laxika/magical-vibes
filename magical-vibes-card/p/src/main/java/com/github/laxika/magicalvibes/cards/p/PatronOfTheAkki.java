package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "115")
public class PatronOfTheAkki extends Card {

    public PatronOfTheAkki() {
        addCastingOption(AlternateHandCast.offering(List.of(
                new ManaCastingCost("{4}{R}{R}"),
                new SacrificePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.GOBLIN))
        )));
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(2, 0));
    }
}
