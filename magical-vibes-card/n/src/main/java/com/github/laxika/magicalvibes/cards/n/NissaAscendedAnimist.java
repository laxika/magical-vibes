package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "175")
public class NissaAscendedAnimist extends Card {

    public NissaAscendedAnimist() {
        CountersOnSource loyalty = new CountersOnSource(CounterType.LOYALTY);
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect(
                        "Phyrexian Horror", loyalty, loyalty, CardColor.GREEN,
                        List.of(CardSubtype.PHYREXIAN, CardSubtype.HORROR), Set.of(), Set.of()
                )),
                "+1: Create an X/X green Phyrexian Horror creature token, where X is Nissa's loyalty."
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new DestroyTargetPermanentEffect()),
                "−1: Destroy target artifact or enchantment.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )),
                        "Target must be an artifact or enchantment"
                )
        ));

        PermanentCount forests = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(
                        new BoostAllOwnCreaturesEffect(forests, forests),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES)
                ),
                "−7: Until end of turn, creatures you control get +1/+1 for each Forest you control "
                        + "and gain trample."
        ));
    }
}
