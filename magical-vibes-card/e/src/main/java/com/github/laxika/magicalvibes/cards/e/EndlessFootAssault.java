package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttackingEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "8")
public class EndlessFootAssault extends Card {

    public EndlessFootAssault() {
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{1}{W}")));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenCopyOfSourceEffect(false, new RepeatedAdditionalCostCount("{1}{W}")));
        addTrigger(Triggers.youAttack(),
                new CreateTokensAttackingEachOpponentEffect(
                        new CreateTokenEffect(1, "Ninja", 1, 1, CardColor.BLACK,
                                List.of(CardSubtype.NINJA), true)));
    }
}
