package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "207")
public class VituGhaziGuildmage extends Card {

    public VituGhaziGuildmage() {
        // {4}{G}{W}: Create a 3/3 green Centaur creature token.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}{W}",
                List.of(new CreateTokenEffect("Centaur", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.CENTAUR), Set.of(), Set.of())),
                "{4}{G}{W}: Create a 3/3 green Centaur creature token."
        ));

        // {2}{G}{W}: Populate.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{W}",
                List.of(new PopulateEffect()),
                "{2}{G}{W}: Populate."
        ));
    }
}
