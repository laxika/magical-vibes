package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "67")
@CardRegistration(set = "M3C", collectorNumber = "119")
public class SageOfTheMaze extends Card {

    public SageOfTheMaze() {
        // {T}: Add two mana in any combination of colors.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardAnyColorManaEffect(2, true)),
                "{T}: Add two mana in any combination of colors."
        ));

        // {T}: Until end of turn, target land you control becomes an X/X Citizen creature with haste
        // in addition to its other types, where X is twice the number of Gates you control. Activate only as a sorcery.
        PermanentCount gatesYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.GATE), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AnimatePermanentsEffect(
                        new Scaled(gatesYouControl, 2),
                        new Scaled(gatesYouControl, 2),
                        List.of(CardSubtype.CITIZEN), Set.of(Keyword.HASTE),
                        null, Set.of(CardType.CREATURE),
                        GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN, null, Set.of())),
                "{T}: Until end of turn, target land you control becomes an X/X Citizen creature with haste "
                        + "in addition to its other types, where X is twice the number of Gates you control. "
                        + "Activate only as a sorcery.",
                TargetFilters.landYouControl(), null, null, ActivationTimingRestriction.SORCERY_SPEED
        ));

        // Tap an untapped Gate you control: Untap this creature.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.GATE)),
                        new UntapPermanentsEffect(TapUntapScope.SELF)),
                "Tap an untapped Gate you control: Untap this creature."
        ));
    }
}
