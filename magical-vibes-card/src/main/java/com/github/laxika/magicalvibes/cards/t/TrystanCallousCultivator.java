package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "ECL", collectorNumber = "199")
@CardRegistration(set = "ECL", collectorNumber = "291")
public class TrystanCallousCultivator extends Card {

    public TrystanCallousCultivator() {
        setBackFaceCard(new TrystanPenitentCuller());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, millThenGainLife());
        addEffect(EffectSlot.ON_TRANSFORM_TO_FRONT_FACE, millThenGainLife());
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{B}", new TransformSelfEffect(),
                        "Pay {B} to transform Trystan?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "TrystanPenitentCuller";
    }

    private CardEffect millThenGainLife() {
        return SequenceEffect.of(
                new MillEffect(3, MillRecipient.CONTROLLER),
                new ConditionalEffect(
                        new GraveyardCardThreshold(1, new CardSubtypePredicate(CardSubtype.ELF)),
                        new GainLifeEffect(2)));
    }
}
