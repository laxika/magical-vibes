package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "285")
public class VituGhaziTheCityTree extends Card {

    public VituGhaziTheCityTree() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {2}{G}{W}, {T}: Create a 1/1 green Saproling creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}{W}",
                List.of(new CreateTokenEffect("Saproling", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SAPROLING), Set.of(), Set.of())),
                "{2}{G}{W}, {T}: Create a 1/1 green Saproling creature token."
        ));
    }
}
