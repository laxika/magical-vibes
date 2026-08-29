package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AER", collectorNumber = "129")
public class HiddenStockpile extends Card {

    public HiddenStockpile() {
        CreateTokenEffect servoToken = new CreateTokenEffect(
                1, "Servo", 1, 1, null,
                List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new PermanentLeftBattlefieldUnderYourControlThisTurn(), servoToken));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeCreatureCost(), new ScryEffect(1)),
                "{1}, Sacrifice a creature: Scry 1."
        ));
    }
}
