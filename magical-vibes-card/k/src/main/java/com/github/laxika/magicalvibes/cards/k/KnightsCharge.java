package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "333")
@CardRegistration(set = "ELD", collectorNumber = "328")
public class KnightsCharge extends Card {

    public KnightsCharge() {
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.KNIGHT)),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{W}{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new ReturnCardsFromControllerGraveyardToBattlefieldEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardSubtypePredicate(CardSubtype.KNIGHT))),
                                new Fixed(Integer.MAX_VALUE), true)
                ),
                "{6}{W}{B}, Sacrifice this enchantment: Return all Knight creature cards from your graveyard to the battlefield."
        ));
    }
}
