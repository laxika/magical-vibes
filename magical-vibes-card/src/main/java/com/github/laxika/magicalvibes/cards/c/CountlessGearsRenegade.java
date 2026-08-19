package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AER", collectorNumber = "13")
public class CountlessGearsRenegade extends Card {

    public CountlessGearsRenegade() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new PermanentLeftBattlefieldUnderYourControlThisTurn(),
                        new CreateTokenEffect(1, "Servo", 1, 1, null,
                                List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT))));
    }
}
