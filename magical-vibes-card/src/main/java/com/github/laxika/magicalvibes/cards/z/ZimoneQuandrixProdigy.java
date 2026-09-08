package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "250")
public class ZimoneQuandrixProdigy extends Card {

    public ZimoneQuandrixProdigy() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land", true),
                        "Put a land card from your hand onto the battlefield tapped?")),
                "{1}, {T}: You may put a land card from your hand onto the battlefield tapped."
        ));

        ControlsPermanentCount eightLands = new ControlsPermanentCount(8, new PermanentIsLandPredicate());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(
                        new ConditionalEffect(eightLands, new DrawCardEffect(2)),
                        new ConditionalEffect(new NotCondition(eightLands), new DrawCardEffect(1))
                ),
                "{4}, {T}: Draw a card. If you control eight or more lands, draw two cards instead."
        ));
    }
}
