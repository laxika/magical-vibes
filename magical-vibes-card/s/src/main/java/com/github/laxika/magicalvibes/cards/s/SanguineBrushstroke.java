package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "32")
@CardRegistration(set = "MB2", collectorNumber = "260")
public class SanguineBrushstroke extends Card {

    public SanguineBrushstroke() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                CreateTokenEffect.ofBloodToken(1),
                new ConjureCardNamedOntoBattlefieldEffect("AVR", "86")));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD),
                                new PermanentIsTokenPredicate())),
                        SequenceEffect.of(
                                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                                new GainLifeEffect(1))));
    }
}
