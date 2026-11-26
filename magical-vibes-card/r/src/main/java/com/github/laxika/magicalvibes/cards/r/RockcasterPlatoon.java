package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "23")
public class RockcasterPlatoon extends Card {

    public RockcasterPlatoon() {
        // {4}{G}: This creature deals 2 damage to each creature with flying and each player.
        addActivatedAbility(new ActivatedAbility(false, "{4}{G}",
                List.of(SequenceEffect.simultaneous(
                        new DealDamageToEachMatchingPermanentEffect(2,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasKeywordPredicate(Keyword.FLYING))),
                                EachPermanentScope.ALL_PLAYERS),
                        DealDamageEffect.to(Subjects.eachPlayer(), 2))),
                "{4}{G}: This creature deals 2 damage to each creature with flying and each player."));
    }
}
