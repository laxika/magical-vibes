package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "85")
public class KeeperOfKookus extends Card {

    public KeeperOfKookus() {
        // {R}: This creature gains protection from red until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new GrantProtectionFromColorUntilEndOfTurnEffect(CardColor.RED, GrantScope.SELF)),
                "{R}: This creature gains protection from red until end of turn."
        ));
    }
}
