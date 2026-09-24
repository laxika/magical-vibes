package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1754")
@CardRegistration(set = "TLE", collectorNumber = "309")
public class DeadlyRollick extends Card {

    public DeadlyRollick() {
        addCastingOption(new AlternateHandCast(List.of(), new ControlledCommanderAsCast(), false));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
