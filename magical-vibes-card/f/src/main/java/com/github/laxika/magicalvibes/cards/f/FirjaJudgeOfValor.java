package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "209")
@CardRegistration(set = "KHM", collectorNumber = "322")
@CardRegistration(set = "MUL", collectorNumber = "40")
@CardRegistration(set = "MUL", collectorNumber = "105")
@CardRegistration(set = "MUL", collectorNumber = "170")
public class FirjaJudgeOfValor extends Card {

    public FirjaJudgeOfValor() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(3, 1));
    }
}
