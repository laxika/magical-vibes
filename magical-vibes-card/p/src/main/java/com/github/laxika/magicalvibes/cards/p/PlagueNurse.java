package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "179")
public class PlagueNurse extends Card {

    public PlagueNurse() {
        addStaticEffect(new ToxicEffect(2));

        var toxicOtherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentHasKeywordPredicate(Keyword.TOXIC),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}",
                List.of(
                        new GrantKeywordEffect(Keyword.TOXIC, GrantScope.OWN_CREATURES,
                                new PermanentHasKeywordPredicate(Keyword.TOXIC)),
                        new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(StaticAbility.of(new ToxicEffect(1)),
                                toxicOtherCreature)),
                "{2}{G}: Each other creature you control with toxic gains toxic 1 until end of turn. Activate only once each turn.",
                1));
    }
}
