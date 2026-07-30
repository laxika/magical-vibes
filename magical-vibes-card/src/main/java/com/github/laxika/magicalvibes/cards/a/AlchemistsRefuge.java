package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToSpellsThisTurnEffect;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "225")
public class AlchemistsRefuge extends Card {

    public AlchemistsRefuge() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {G}{U}, {T}: You may cast spells this turn as though they had flash.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{U}",
                List.of(new GrantFlashToSpellsThisTurnEffect()),
                "{G}{U}, {T}: You may cast spells this turn as though they had flash."
        ));
    }
}
