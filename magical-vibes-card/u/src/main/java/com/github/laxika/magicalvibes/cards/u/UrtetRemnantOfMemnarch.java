package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONC", collectorNumber = "28")
@CardRegistration(set = "ONC", collectorNumber = "42")
public class UrtetRemnantOfMemnarch extends Card {

    private static final PermanentHasSubtypePredicate MYR_PERMANENT =
            new PermanentHasSubtypePredicate(CardSubtype.MYR);

    public UrtetRemnantOfMemnarch() {
        // Whenever you cast a Myr spell, create a 1/1 colorless Myr artifact creature token.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardSubtypePredicate(CardSubtype.MYR),
                        List.of(new CreateTokenEffect(
                                1, "Myr", 1, 1, null,
                                List.of(CardSubtype.MYR), Set.of(), Set.of(CardType.ARTIFACT)))));

        // At the beginning of combat on your turn, untap each Myr you control.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, MYR_PERMANENT));

        // {W}{U}{B}{R}{G}, {T}: Put three +1/+1 counters on each Myr you control.
        addActivatedAbility(new ActivatedAbility(
                true, "{W}{U}{B}{R}{G}",
                List.of(new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 3, MYR_PERMANENT)),
                "{W}{U}{B}{R}{G}, {T}: Put three +1/+1 counters on each Myr you control. Activate only during your turn.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN));
    }
}
