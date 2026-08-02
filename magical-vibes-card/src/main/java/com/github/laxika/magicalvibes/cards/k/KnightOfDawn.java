package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "26")
public class KnightOfDawn extends Card {

    public KnightOfDawn() {
        // {W}{W}: This creature gains protection from the color of your choice until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{W}",
                List.of(new GrantProtectionChoiceUntilEndOfTurnEffect(GrantScope.SELF)),
                "{W}{W}: This creature gains protection from the color of your choice until end of turn."
        ));
    }
}
