package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "255")
public class KavaronMemorialWorld extends Card {

    public KavaronMemorialWorld() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate(), true, true),
                        new PutCountersOnSelfEffect(CounterType.CHARGE, new ChosenPermanentPower())
                ),
                "Tap another creature you control: Put charge counters equal to its power on Kavaron, Memorial World.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land", false),
                        new CreateTokenEffect("Robot", 2, 2, null,
                                List.of(CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT)),
                        new BoostAllOwnCreaturesEffect(1, 0),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES)
                ),
                "{1}{R}, {T}, Sacrifice a land: Create a 2/2 colorless Robot artifact creature token, then "
                        + "creatures you control get +1/+0 and gain haste until end of turn."
        ).withRequiredSourceCounters(CounterType.CHARGE, 12));
    }
}
