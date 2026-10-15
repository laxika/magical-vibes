package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "ECL", collectorNumber = "124")
@CardRegistration(set = "ECL", collectorNumber = "290")
public class AshlingRekindled extends Card {

    public AshlingRekindled() {
        setBackFaceCard(new AshlingRimebound());

        MayEffect discardAndDraw = new MayEffect(
                new DiscardAndDrawCardEffect(), "Discard a card to draw a card?");
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, discardAndDraw);
        addEffect(EffectSlot.ON_TRANSFORM_TO_FRONT_FACE, discardAndDraw);

        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{U}", new TransformSelfEffect(),
                        "Pay {U} to transform Ashling?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "AshlingRimebound";
    }
}
