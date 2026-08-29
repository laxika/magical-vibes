package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WaywardDisciple;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "34")
public class PiousEvangel extends Card {

    public PiousEvangel() {
        setBackFaceCard(new WaywardDisciple());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(1));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEffect(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificePermanentCost(new PermanentTruePredicate(), "Sacrifice another permanent"),
                        new TransformSelfEffect()),
                "{2}, {T}, Sacrifice another permanent: Transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "WaywardDisciple";
    }
}
