package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "32")
@CardRegistration(set = "MB2", collectorNumber = "260")
public class SanguineBrushstroke extends Card {

    public SanguineBrushstroke() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                CreateTokenEffect.ofBloodToken(1),
                new ConjureCardNamedOntoBattlefieldEffect("AVR", "86")));
        addTrigger(Triggers.sacrificed(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD),
                                new PermanentIsTokenPredicate())),
                        ObjectRelation.YOU_CONTROL),
                SequenceEffect.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT), new GainLifeEffect(1)));
    }
}
