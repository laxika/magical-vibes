package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerControlsMoreCreaturesThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EXO", collectorNumber = "112")
public class KeeperOfTheBeasts extends Card {

    public KeeperOfTheBeasts() {
        // {G}, {T}: Choose target opponent who controls more creatures than you do as you activate
        // this ability. Create a 2/2 green Beast creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new CreateTokenEffect(
                        "Beast", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAST), Set.of(), Set.of())),
                "{G}, {T}: Choose target opponent who controls more creatures than you do as you activate this ability. "
                        + "Create a 2/2 green Beast creature token.",
                new PlayerPredicateTargetFilter(
                        new PlayerControlsMoreCreaturesThanControllerPredicate(),
                        "Target opponent must control more creatures than you"
                )
        ));
    }
}
