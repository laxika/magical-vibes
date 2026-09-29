package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "132")
@CardRegistration(set = "PIP", collectorNumber = "660")
@CardRegistration(set = "PIP", collectorNumber = "1059")
public class EnduranceBobblehead extends Card {

    public EnduranceBobblehead() {
        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        // Up to X target creatures you control get +1/+0 and gain indestructible until end of turn,
        // where X is the number of Bobbleheads you control as you activate this ability.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new BoostTargetCreatureEffect(1, 0),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET)
                ),
                "{3}, {T}: Up to X target creatures you control get +1/+0 and gain indestructible until end of turn, where X is the number of Bobbleheads you control as you activate this ability. Activate only as a sorcery.",
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
