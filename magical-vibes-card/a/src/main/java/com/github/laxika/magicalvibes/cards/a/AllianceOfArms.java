package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "CMD", collectorNumber = "4")
public class AllianceOfArms extends Card {

    public AllianceOfArms() {
        addSpellEffect(SequenceEffect.of(
                EachPlayerPaysAnyManaEffect.joinForces(),
                new EachPlayerCreatesTokenEffect(new CreateTokenEffect("Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.SOLDIER), Set.of(), Set.of())
                        .withAmount(AmountThisWay.manaPaid()))));
    }
}
