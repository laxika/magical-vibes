package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;

@CardRegistration(set = "FIN", collectorNumber = "91")
public class CecilDarkKnight extends Card {

    public CecilDarkKnight() {
        setBackFaceCard(new CecilRedeemedPaladin());

        addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE, SequenceEffect.of(
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.CONTROLLER),
                ConditionalEffect.unless(
                        new ControllerLifeAtMost(GameData.STARTING_LIFE_TOTAL / 2),
                        SequenceEffect.of(
                                new UntapPermanentsEffect(TapUntapScope.SELF),
                                new TransformToBackFaceEffect()))));
    }

    @Override
    public String getBackFaceClassName() {
        return "CecilRedeemedPaladin";
    }
}
