package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "175")
@CardRegistration(set = "TMT", collectorNumber = "221")
@CardRegistration(set = "TMT", collectorNumber = "290")
@CardRegistration(set = "TMT", collectorNumber = "300")
public class KrangUtromWarlord extends Card {

    public KrangUtromWarlord() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.FLYING, Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE, Keyword.HASTE),
                GrantScope.OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()
                ))
        ));
    }
}
