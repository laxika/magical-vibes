package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "65")
public class QiqirnMerchant extends Card {

    public QiqirnMerchant() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{1}, {T}: Draw a card, then discard a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}",
                List.of(
                        new SacrificeSelfCost(),
                        new ReduceActivationCostEffect(new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.TOWN), CountScope.CONTROLLER)),
                        new DrawCardEffect(3)
                ),
                "{7}, {T}, Sacrifice this creature: Draw three cards. This ability costs {1} less to activate for each Town you control."
        ));
    }
}
