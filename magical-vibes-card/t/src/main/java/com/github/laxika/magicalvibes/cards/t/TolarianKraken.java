package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "80")
public class TolarianKraken extends Card {

    public TolarianKraken() {
        addTrigger(Triggers.youDrawCard(), new MayPayManaEffect("{1}",
                new QueueReflexiveAbilityEffect(new MayEffect(
                        new TapOrUntapTargetPermanentEffect(new PermanentIsCreaturePredicate()),
                        "Tap or untap target creature?")),
                "Pay {1} to use its ability?"));
    }
}
