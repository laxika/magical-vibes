package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "180")
public class IzoniThousandEyed extends Card {

    public IzoniThousandEyed() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                CardType.CREATURE,
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER),
                "Insect", 1, 1, CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.GREEN),
                List.of(CardSubtype.INSECT), Set.of(), Set.of(), false, false, Map.of(), List.of(),
                false, false, false, 0, Set.of()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{G}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new GainLifeEffect(1),
                        new DrawCardEffect(1)
                ),
                "{B}{G}, Sacrifice another creature: You gain 1 life and draw a card."
        ));
    }
}
