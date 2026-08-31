package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BakeryRaid;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "174")
public class HollowScavenger extends Card {

    public HollowScavenger() {
        setBackFaceCard(new BakeryRaid());
        addCastingOption(new AdventureCast("{G}"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.FOOD),
                                "Sacrifice a Food"),
                        new BoostSelfEffect(2, 2)),
                "{1}, Sacrifice a Food: This creature gets +2/+2 until end of turn. Activate only once each turn.",
                1
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "BakeryRaid";
    }
}
