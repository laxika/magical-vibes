package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceToHandAtNextUntapEffect;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "167")
public class UndiscoveredParadise extends Card {

    public UndiscoveredParadise() {
        // {T}: Add one mana of any color. During your next untap step, as you untap your
        // permanents, return this land to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardAnyColorManaEffect(), new ReturnSourceToHandAtNextUntapEffect()),
                "{T}: Add one mana of any color. During your next untap step, as you untap your permanents, return this land to its owner's hand."
        ));
    }
}
