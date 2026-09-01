package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "63")
public class MechanAssembler extends Card {

    public MechanAssembler() {
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new OncePerTurnTriggerEffect(new CreateTokenEffect("Robot", 2, 2, null,
                        List.of(CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT))));
    }
}
