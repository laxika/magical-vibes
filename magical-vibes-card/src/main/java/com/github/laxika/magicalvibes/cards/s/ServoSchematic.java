package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AER", collectorNumber = "176")
public class ServoSchematic extends Card {

    public ServoSchematic() {
        CreateTokenEffect servo = new CreateTokenEffect(
                1, "Servo", 1, 1, null,
                List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, servo);
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, servo);
    }
}
