package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "WTH", collectorNumber = "131")
public class LiegeOfTheHollows extends Card {

    public LiegeOfTheHollows() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                EachPlayerPaysAnyManaEffect.inApnapOrder(),
                new EachPlayerCreatesTokenEffect(new CreateTokenEffect("Squirrel", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SQUIRREL), Set.of(), Set.of())
                        .withAmount(AmountThisWay.manaPaidByYou()))));
    }
}
