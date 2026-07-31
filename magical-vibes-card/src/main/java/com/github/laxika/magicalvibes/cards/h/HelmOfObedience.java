package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillUntilCreatureThenReanimateEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "121")
public class HelmOfObedience extends Card {

    public HelmOfObedience() {
        // {X}, {T}: Target opponent mills a card, then repeats this process until a creature card or
        // X cards have been put into their graveyard this way, whichever comes first. If one or more
        // creature cards were put into that graveyard this way, sacrifice this artifact and put one
        // of them onto the battlefield under your control. X can't be 0. The paid X rides on the
        // stack entry's xValue, which the effect handler reads.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new MillUntilCreatureThenReanimateEffect()),
                "{X}, {T}: Target opponent mills a card, then repeats this process until a creature "
                        + "card or X cards have been put into their graveyard this way, whichever comes "
                        + "first. If one or more creature cards were put into that graveyard this way, "
                        + "sacrifice this artifact and put one of them onto the battlefield under your "
                        + "control. X can't be 0.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                ),
                null,
                null,
                null));
    }
}
