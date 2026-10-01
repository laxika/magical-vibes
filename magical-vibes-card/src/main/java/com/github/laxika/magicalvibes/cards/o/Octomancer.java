package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLC", collectorNumber = "37")
@CardRegistration(set = "BLC", collectorNumber = "69")
public class Octomancer extends Card {

    public Octomancer() {
        addEffect(EffectSlot.STATIC, new GiftEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new GiftPromised(),
                TargetOpponentCreatesTokenEffect.gift(octopusToken())));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTokenPredicate(),
                        new PermanentEnteredBattlefieldThisTurnPredicate())),
                "Target must be a creature token that entered this turn"))
                .addEffect(EffectSlot.END_STEP_TRIGGERED, new CreateTokenCopyOfTargetPermanentEffect());
    }

    private static CreateTokenEffect octopusToken() {
        return new CreateTokenEffect(
                "Octopus", 8, 8, CardColor.BLUE, List.of(CardSubtype.OCTOPUS), Set.of(), Set.of());
    }
}
