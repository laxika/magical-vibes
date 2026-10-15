package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "76")
public class StormscapeMaster extends Card {

    public StormscapeMaster() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{W}",
                List.of(new GrantProtectionChoiceUntilEndOfTurnEffect()),
                "{W}{W}, {T}: Target creature gains protection from the color of your choice until end of turn.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}{B}",
                List.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER),
                        new GainLifeEffect(2)
                ),
                "{B}{B}, {T}: Target player loses 2 life and you gain 2 life."
        ));
    }
}
