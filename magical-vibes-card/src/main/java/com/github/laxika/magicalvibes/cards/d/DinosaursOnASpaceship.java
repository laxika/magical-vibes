package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "122")
public class DinosaursOnASpaceship extends Card {

    public DinosaursOnASpaceship() {
        // Other Dinosaurs you control get +1/+1 and have vigilance and trample.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1,
                Set.of(Keyword.VIGILANCE, Keyword.TRAMPLE), GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)));

        // Whenever a time counter is removed from this card while it's exiled, create a 2/2 red
        // and white Dinosaur creature token with flying and haste.
        addEffect(EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE,
                new CreateTokenEffect(CardType.CREATURE, 1, "Dinosaur", 2, 2, CardColor.RED,
                        Set.of(CardColor.RED, CardColor.WHITE), List.of(CardSubtype.DINOSAUR),
                        Set.of(Keyword.FLYING, Keyword.HASTE), Set.of(), false, false,
                        Map.of(), List.of(), false, false, false, 0, Set.of()));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}{W}",
                List.of(),
                "Suspend 4—{3}{R}{W}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
