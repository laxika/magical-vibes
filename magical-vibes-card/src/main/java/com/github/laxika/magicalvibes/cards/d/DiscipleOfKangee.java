package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "3")
public class DiscipleOfKangee extends Card {

    public DiscipleOfKangee() {
        addActivatedAbility(new ActivatedAbility(
                true, "{U}",
                List.of(
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET),
                        new GrantColorUntilEndOfTurnEffect(CardColor.BLUE)
                ),
                "{U}, {T}: Target creature gains flying and becomes blue until end of turn.",
                TargetFilters.creature()
        ));
    }
}
