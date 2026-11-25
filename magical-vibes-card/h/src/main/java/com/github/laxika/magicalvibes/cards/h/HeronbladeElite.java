package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MIC", collectorNumber = "26")
@CardRegistration(set = "MIC", collectorNumber = "64")
public class HeronbladeElite extends Card {

    public HeronbladeElite() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.HUMAN))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(new SourcePower())),
                "{T}: Add X mana of any one color, where X is Heronblade Elite's power."
        ));
    }
}
