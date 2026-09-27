package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantPayLifeEqualToSpellManaValueForNextSpellEffect;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "43")
public class MarshlandBloodcaster extends Card {

    public MarshlandBloodcaster() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(new GrantPayLifeEqualToSpellManaValueForNextSpellEffect()),
                "{1}{B}, {T}: Rather than pay the mana cost of the next spell you cast this turn, you may pay life equal to that spell's mana value."
        ));
    }
}
