package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "243")
public class KessigWolfRun extends Card {

    public KessigWolfRun() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {X}{R}{G}, {T}: Target creature gets +X/+0 and gains trample until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{R}{G}",
                List.of(
                        new BoostTargetCreatureEffect(new XValue(), new Fixed(0)),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)
                ),
                "{X}{R}{G}, {T}: Target creature gets +X/+0 and gains trample until end of turn.",
                TargetFilters.creature()
        ));
    }
}
