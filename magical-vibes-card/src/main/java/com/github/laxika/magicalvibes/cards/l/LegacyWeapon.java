package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "330")
public class LegacyWeapon extends Card {

    public LegacyWeapon() {
        setShufflesIntoLibraryFromGraveyard(true);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{U}{B}{R}{G}",
                List.of(new ExileTargetPermanentEffect()),
                true,
                "{W}{U}{B}{R}{G}: Exile target permanent."
        ));
    }
}
