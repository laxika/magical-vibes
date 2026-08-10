package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandOnTopOfLibraryCost;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EXO", collectorNumber = "15")
public class Penance extends Card {

    public Penance() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PutCardFromHandOnTopOfLibraryCost(),
                        PreventDamageFromChosenSourceEffect.nextDamageToYou(
                                new PermanentColorInPredicate(Set.of(CardColor.BLACK, CardColor.RED)),
                                "black or red")),
                "Put a card from your hand on top of your library: The next time a black or red source "
                        + "of your choice would deal damage this turn, prevent that damage."
        ));
    }
}
