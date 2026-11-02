package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "91")
@CardRegistration(set = "FIN", collectorNumber = "380")
@CardRegistration(set = "FIN", collectorNumber = "445")
@CardRegistration(set = "FIN", collectorNumber = "525")
public class CecilDarkKnight extends Card {

    public CecilDarkKnight() {
        setBackFaceCard(new CecilRedeemedPaladin());

        addTrigger(Triggers.dealsDamage(), SequenceEffect.of(
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.CONTROLLER),
                ConditionalEffect.unless(
                        ControllerLifeAtMost.atHalfStartingLife(),
                        SequenceEffect.of(
                                new UntapPermanentsEffect(TapUntapScope.SELF),
                                new TransformToBackFaceEffect()))));
    }

    @Override
    public String getBackFaceClassName() {
        return "CecilRedeemedPaladin";
    }
}
