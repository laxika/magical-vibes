package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "52")
public class LivingTsunami extends Card {

    public LivingTsunami() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), UnlessPaysEffect.unlessYouPay(
                ReturnPermanentToHandCost.youControl(new PermanentIsLandPredicate(), "land you control"),
                new SacrificeSelfEffect()));
    }
}
