package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandSharingNameWithSpellCastThisTurnEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "264")
public class TwinningGlass extends Card {

    public TwinningGlass() {
        // {1}, {T}: You may cast a spell from your hand without paying its mana cost if it has the
        // same name as a spell that was cast this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new MayCastFromHandSharingNameWithSpellCastThisTurnEffect()),
                "{1}, {T}: You may cast a spell from your hand without paying its mana cost if it has the same name as a spell that was cast this turn."
        ));
    }
}
