package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "201")
public class CogworkersPuzzleknot extends Card {

    public CogworkersPuzzleknot() {
        CreateTokenEffect servoToken = new CreateTokenEffect(
                1, "Servo", 1, 1, null,
                List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, servoToken);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new SacrificeSelfCost(), servoToken),
                "{1}{W}, Sacrifice this artifact: Create a 1/1 colorless Servo artifact creature token."
        ));
    }
}
