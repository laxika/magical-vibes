package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "100")
public class PiperOfTheSwarm extends Card {

    public PiperOfTheSwarm() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.MENACE,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.RAT)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(new CreateTokenEffect(
                        1,
                        "Rat",
                        1,
                        1,
                        CardColor.BLACK,
                        List.of(CardSubtype.RAT),
                        Set.of(),
                        Set.of())),
                "{1}{B}, {T}: Create a 1/1 black Rat creature token."));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}{B}",
                List.of(
                        new SacrificeMultiplePermanentsCost(3, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.RAT)))),
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT)),
                "{2}{B}{B}, {T}, Sacrifice three Rats: Gain control of target creature.",
                TargetFilters.creature()));
    }
}
