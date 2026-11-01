package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "127")
public class BelbesPortal extends Card {

    public BelbesPortal() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardHasSourceChosenSubtypePredicate(),
                                "creature of the chosen type"),
                        "Put a creature card of the chosen type from your hand onto the battlefield?"
                )),
                "{3}, {T}: You may put a creature card of the chosen type from your hand onto the battlefield."
        ));
    }
}
