package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ARN", collectorNumber = "69")
public class SandalsOfAbdallah extends Card {

    public SandalsOfAbdallah() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new GrantKeywordEffect(Keyword.ISLANDWALK, GrantScope.TARGET),
                        new ResolveEffectOnTargetDeathThisTurnEffect(
                                new DestroyReferencedPermanentEffect(PermanentReference.SOURCE))),
                "{2}, {T}: Target creature gains islandwalk until end of turn. When that creature "
                        + "dies this turn, destroy this artifact.",
                TargetFilters.creature()));
    }
}
