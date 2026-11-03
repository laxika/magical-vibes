package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessSacrificesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DRK", collectorNumber = "31")
@CardRegistration(set = "ME3", collectorNumber = "44")
public class ManaVortex extends Card {

    public ManaVortex() {
        addTrigger(Triggers.youCastThisSpell(),
                new CounterUnlessSacrificesEffect(new PermanentIsLandPredicate(), "land"));
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new SacrificePermanentsEffect(
                1, new PermanentIsLandPredicate(), SacrificeRecipient.ACTIVE_PLAYER));
        addTrigger(Triggers.battlefieldHasAtMost(0, new PermanentIsLandPredicate()), new SacrificeSelfEffect());
    }
}
