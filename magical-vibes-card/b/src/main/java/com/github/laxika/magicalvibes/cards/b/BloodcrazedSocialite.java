package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "96")
@CardRegistration(set = "DBL", collectorNumber = "363")
public class BloodcrazedSocialite extends Card {

    public BloodcrazedSocialite() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofBloodToken(1));
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.BLOOD),
                                        new PermanentIsTokenPredicate())),
                                Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(),
                                new BoostSelfEffect(2, 2))),
                "Sacrifice a Blood token?"));
    }
}
