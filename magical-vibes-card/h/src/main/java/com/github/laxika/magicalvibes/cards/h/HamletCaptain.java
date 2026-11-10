package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "187")
@CardRegistration(set = "INR", collectorNumber = "201")
@CardRegistration(set = "INR", collectorNumber = "413")
@CardRegistration(set = "EMN", collectorNumber = "161")
@CardRegistration(set = "SIR", collectorNumber = "200")
public class HamletCaptain extends Card {

    public HamletCaptain() {
        // Whenever Hamlet Captain attacks or blocks, other Humans you control get +1/+1 until end of turn.
        var otherHumansFilter = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        var boostOtherHumans = new BoostAllOwnCreaturesEffect(1, 1, otherHumansFilter);
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), TriggerOptions.ONE_OR_MORE, boostOtherHumans);
    }
}
