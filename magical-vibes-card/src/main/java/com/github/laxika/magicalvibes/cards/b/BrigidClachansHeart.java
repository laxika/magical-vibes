package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.BrigidDounsMind;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "7")
public class BrigidClachansHeart extends Card {

    public BrigidClachansHeart() {
        setBackFaceCard(new BrigidDounsMind());

        CreateTokenEffect kithkinToken = new CreateTokenEffect(
                "Kithkin", 1, 1, CardColor.GREEN,
                Set.of(CardColor.GREEN, CardColor.WHITE),
                List.of(CardSubtype.KITHKIN));

        // Whenever this creature enters or transforms into Brigid, Clachan's Heart, create a 1/1 green and white Kithkin creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, kithkinToken);
        addEffect(EffectSlot.ON_TRANSFORM_TO_FRONT_FACE, kithkinToken);

        // At the beginning of your first main phase, you may pay {G}. If you do, transform Brigid.
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{G}", new TransformSelfEffect(),
                        "Pay {G} to transform Brigid?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "BrigidDounsMind";
    }
}
