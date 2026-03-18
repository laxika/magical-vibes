package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "MBS", collectorNumber = "71")
public class MetallicMastery extends Card {

    public MetallicMastery() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Target must be an artifact"
        ))
                .addEffect(EffectSlot.SPELL, new GainControlOfTargetPermanentUntilEndOfTurnEffect())
                .addEffect(EffectSlot.SPELL, new UntapTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
