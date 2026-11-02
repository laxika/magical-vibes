package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "99")
@CardRegistration(set = "SOC", collectorNumber = "226")
public class SmotheringAbomination extends Card {

    public SmotheringAbomination() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.CONTROLLER));
        addTrigger(Triggers.sacrificed(new PermanentIsCreaturePredicate(), ObjectRelation.YOU_CONTROL),
                new DrawCardEffect());
    }
}
