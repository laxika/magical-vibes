package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "80")
public class SoldierOfFortune extends Card {

    public SoldierOfFortune() {
        // {R}, {T}: Target player shuffles their library.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new ShuffleLibraryEffect(true)),
                "{R}, {T}: Target player shuffles their library."
        ));
    }
}
