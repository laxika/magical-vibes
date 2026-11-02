package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.n.NeoExdeathDimensionsEnd;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "220")
@CardRegistration(set = "FIN", collectorNumber = "485")
public class ExdeathVoidWarlock extends Card {

    public ExdeathVoidWarlock() {
        setBackFaceCard(new NeoExdeathDimensionsEnd());

        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new GraveyardCardThreshold(6, new CardIsPermanentPredicate()),
                new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "NeoExdeathDimensionsEnd";
    }
}
