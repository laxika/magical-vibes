package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "151")
public class CultivatorOfBlades extends Card {

    public CultivatorOfBlades() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put two +1/+1 counters on Cultivator of Blades",
                        new PutCountersOnSourceEffect(1, 1, 2)
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Create two 1/1 colorless Servo artifact creature tokens",
                        new CreateTokenEffect(2, "Servo", 1, 1, null,
                                List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT))
                )
        )));

        var otherAttackers = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new BoostAllCreaturesEffect(new SourcePower(), new SourcePower(), otherAttackers),
                "Have other attacking creatures get +X/+X until end of turn?"
        ));
    }
}
