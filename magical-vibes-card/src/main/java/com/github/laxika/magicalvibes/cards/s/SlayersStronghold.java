package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "229")
public class SlayersStronghold extends Card {

    public SlayersStronghold() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {R}{W}, {T}: Target creature gets +2/+0 and gains vigilance and haste until end of turn.
        addActivatedAbility(new ActivatedAbility(true, "{R}{W}",
                List.of(
                        new BoostTargetCreatureEffect(2, 0),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)
                ),
                "{R}{W}, {T}: Target creature gets +2/+0 and gains vigilance and haste until end of turn.",
                TargetFilters.creature()));
    }
}
