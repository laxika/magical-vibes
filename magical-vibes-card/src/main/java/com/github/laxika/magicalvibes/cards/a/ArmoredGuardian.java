package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "230")
public class ArmoredGuardian extends Card {

    public ArmoredGuardian() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}{W}",
                List.of(new GrantProtectionChoiceUntilEndOfTurnEffect()),
                "{1}{W}{W}: Target creature you control gains protection from the color of your choice until end of turn.",
                TargetFilters.creatureYouControl()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}{U}",
                List.of(new GrantKeywordEffect(Keyword.SHROUD, GrantScope.SELF)),
                "{1}{U}{U}: This creature gains shroud until end of turn."
        ));
    }
}
