package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "50")
public class PasswallAdept extends Card {

    public PasswallAdept() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{U}", List.of(new MakeCreatureUnblockableEffect()),
                "{2}{U}: Target creature can't be blocked this turn.", TargetFilters.creature()));
    }
}
