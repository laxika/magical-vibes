package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "208")
@CardRegistration(set = "MSC", collectorNumber = "449")
@CardRegistration(set = "LCC", collectorNumber = "68")
@CardRegistration(set = "LCC", collectorNumber = "100")
public class ProgenitorsIcon extends Card {

    public ProgenitorsIcon() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantFlashToNextMatchingSpellEffect(new CardHasSourceChosenSubtypePredicate(false))),
                "{T}: The next spell of the chosen type you cast this turn can be cast as though it had flash."
        ));
    }
}
