package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "100")
@CardRegistration(set = "MSC", collectorNumber = "422")
public class UltronUnlimited extends Card {

    public UltronUnlimited() {
        addEffect(EffectSlot.ON_ATTACK, new DrawDiscardAndConniveEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_CONNIVES, new MayPayManaEffect(
                "{1}",
                new CreateTokenEffect(
                        "Robot",
                        2,
                        2,
                        null,
                        List.of(CardSubtype.ROBOT, CardSubtype.VILLAIN),
                        Set.of(),
                        Set.of(CardType.ARTIFACT)),
                "Pay {1} to create a Robot Villain token?"));
    }
}
