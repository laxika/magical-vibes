package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromChosenSourceMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "31")
public class GreaterRealmOfPreservation extends Card {

    public GreaterRealmOfPreservation() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new PreventNextDamageFromChosenSourceMatchingEffect(new PermanentColorInPredicate(Set.of(CardColor.BLACK, CardColor.RED)), "black or red")),
                "The next time a black or red source of your choice would deal damage to you this turn, prevent that damage."
        ));
    }
}
