package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "11")
public class Glory extends Card {

    public Glory() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new GrantProtectionChoiceUntilEndOfTurnEffect(GrantScope.OWN_CREATURES)),
                "{2}{W}: Choose a color. Creatures you control gain protection from the chosen color until end of turn."
        ));
    }
}
