package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandBottomThenBoostSelfByManaValueEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "204")
public class GoblinMachinist extends Card {

    public GoblinMachinist() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new RevealUntilNonlandBottomThenBoostSelfByManaValueEffect()),
                "{2}{R}: Reveal cards from the top of your library until you reveal a nonland card. "
                        + "This creature gets +X/+0 until end of turn, where X is that card's mana value. "
                        + "Put the revealed cards on the bottom of your library in any order."));
    }
}
