package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "124")
public class MarrowGnawer extends Card {

    public MarrowGnawer() {
        PermanentPredicate rat = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.RAT)
        ));

        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.FEAR, GrantScope.ALL_CREATURES_INCLUDING_SELF, rat));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(rat, "Sacrifice a Rat", false),
                        new CreateTokenEffect(
                                new PermanentCount(rat, CountScope.CONTROLLER),
                                "Rat", 1, 1, CardColor.BLACK, List.of(CardSubtype.RAT), Set.of(), Set.of())
                ),
                "{T}, Sacrifice a Rat: Create X 1/1 black Rat creature tokens, where X is the number of Rats you control."));
    }
}
