package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "41")
public class RappellingScouts extends Card {

    public RappellingScouts() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}{W}",
                List.of(new GrantProtectionChoiceUntilEndOfTurnEffect(GrantScope.SELF)),
                "{2}{W}{W}: This creature gains protection from the color of your choice until end of turn."
        ));
    }
}
