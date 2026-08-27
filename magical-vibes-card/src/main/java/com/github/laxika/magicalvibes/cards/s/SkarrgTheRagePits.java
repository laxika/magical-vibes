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

@CardRegistration(set = "GPT", collectorNumber = "163")
public class SkarrgTheRagePits extends Card {

    public SkarrgTheRagePits() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {R}{G}, {T}: Target creature gets +1/+1 and gains trample until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}{G}",
                List.of(
                        new BoostTargetCreatureEffect(1, 1),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)
                ),
                "{R}{G}, {T}: Target creature gets +1/+1 and gains trample until end of turn.",
                TargetFilters.creature()
        ));
    }
}
