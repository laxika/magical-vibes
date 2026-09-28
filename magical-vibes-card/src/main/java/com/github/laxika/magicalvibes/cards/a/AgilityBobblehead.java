package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "126")
@CardRegistration(set = "PIP", collectorNumber = "654")
@CardRegistration(set = "PIP", collectorNumber = "1062")
public class AgilityBobblehead extends Card {

    public AgilityBobblehead() {
        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        // Up to X target creatures you control each gain haste until end of turn and can't be
        // blocked this turn except by creatures with haste, where X is the number of Bobbleheads
        // you control as this ability is activated.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET),
                        new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                                new PermanentHasKeywordPredicate(Keyword.HASTE),
                                "creatures with haste")
                ),
                "{3}, {T}: Up to X target creatures you control each gain haste until end of turn and can't be blocked this turn except by creatures with haste, where X is the number of Bobbleheads you control as you activate this ability.",
                TargetFilters.creatureYouControl(),
                null,
                null,
                null,
                List.of(),
                0,
                100
        ).withDynamicMaxTargets(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.BOBBLEHEAD),
                CountScope.CONTROLLER)));
    }
}
