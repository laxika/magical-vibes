package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "245")
@CardRegistration(set = "ORI", collectorNumber = "250")
public class RoguesPassage extends Card {

    public RoguesPassage() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{4}, {T}: Target creature can't be blocked this turn.",
                TargetFilters.creature()
        ));
    }
}
