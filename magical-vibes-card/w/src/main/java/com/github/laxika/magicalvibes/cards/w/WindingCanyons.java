package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeThisTurnEffect;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "167")
public class WindingCanyons extends Card {

    public WindingCanyons() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {2}, {T}: You may cast creature spells this turn as though they had flash.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new GrantFlashToCardTypeThisTurnEffect(CardType.CREATURE)),
                "{2}, {T}: You may cast creature spells this turn as though they had flash."
        ));
    }
}
