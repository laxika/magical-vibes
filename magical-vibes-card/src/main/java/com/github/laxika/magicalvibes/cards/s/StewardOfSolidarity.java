package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "25")
public class StewardOfSolidarity extends Card {

    public StewardOfSolidarity() {
        // {T}, Exert this creature: Create a 1/1 white Warrior creature token with vigilance.
        // Exert ("won't untap during your next untap step") is modeled as SkipNextUntapEffect(SELF),
        // matching the existing exert pattern (Ahn-Crop Crasher / Battlefield Scavenger). The {T} is the
        // tap cost (requiresTap); there is no mana cost.
        addActivatedAbility(new ActivatedAbility(
                true, "",
                List.of(
                        new SkipNextUntapEffect(TapUntapScope.SELF),
                        new CreateTokenEffect(
                                "Warrior", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.WARRIOR),
                                Set.of(Keyword.VIGILANCE), Set.of()
                        )
                ),
                "{T}, Exert this creature: Create a 1/1 white Warrior creature token with vigilance."
        ));
    }
}
