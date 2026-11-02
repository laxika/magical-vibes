package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "100")
@CardRegistration(set = "MSC", collectorNumber = "422")
public class UltronUnlimited extends Card {

    public UltronUnlimited() {
        addTrigger(Triggers.attacks(), new DrawDiscardAndConniveEffect());
        addTrigger(Triggers.creatureYouControlConnives(), new MayPayManaEffect(
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
