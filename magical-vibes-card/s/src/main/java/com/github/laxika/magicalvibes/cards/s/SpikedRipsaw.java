package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "220")
@CardRegistration(set = "DBL", collectorNumber = "487")
public class SpikedRipsaw extends Card {

    public SpikedRipsaw() {
        addStaticEffect(new StaticBoostEffect(3, 3, Subjects.attachedCreature()));
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentHasSubtypePredicate(CardSubtype.FOREST), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(),
                                new BoostSelfEffect(Subjects.triggering(), 0, 0, Keyword.TRAMPLE))),
                "Sacrifice a Forest?"));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
