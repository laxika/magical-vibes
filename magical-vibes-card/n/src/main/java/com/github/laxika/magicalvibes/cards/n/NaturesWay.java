package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "162")
@CardRegistration(set = "DDU", collectorNumber = "20")
@CardRegistration(set = "KLR", collectorNumber = "171")
public class NaturesWay extends Card {

    public NaturesWay() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new GrantKeywordEffect(Set.of(Keyword.VIGILANCE, Keyword.TRAMPLE), Subjects.target()));
        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(DealDamageEffect.bite());
    }
}
