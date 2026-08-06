package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MeldWithNamedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "279")
public class HanweirBattlements extends Card {

    private static final String PARTNER_NAME = "Hanweir Garrison";

    public HanweirBattlements() {
        setBackFaceCard(new HanweirTheWrithingTownship());

        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {R}, {T}: Target creature gains haste until end of turn.
        addActivatedAbility(new ActivatedAbility(true, "{R}",
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                "{R}, {T}: Target creature gains haste until end of turn.",
                TargetFilters.creature()));

        // {3}{R}{R}, {T}: If you both own and control this land and a creature named Hanweir Garrison,
        // exile them, then meld them into Hanweir, the Writhing Township.
        addActivatedAbility(new ActivatedAbility(true, "{3}{R}{R}",
                List.of(new ConditionalEffect(
                        new AllOf(List.of(
                                new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                        new PermanentIsSourceCardPredicate(),
                                        new PermanentOwnedBySourceControllerPredicate()))),
                                new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                        new PermanentNamedPredicate(PARTNER_NAME),
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentOwnedBySourceControllerPredicate()))))),
                        new MeldWithNamedCreatureEffect(PARTNER_NAME))),
                "{3}{R}{R}, {T}: If you both own and control this land and a creature named Hanweir Garrison, "
                        + "exile them, then meld them into Hanweir, the Writhing Township."));
    }

    @Override
    public String getBackFaceClassName() {
        return "HanweirTheWrithingTownship";
    }
}
