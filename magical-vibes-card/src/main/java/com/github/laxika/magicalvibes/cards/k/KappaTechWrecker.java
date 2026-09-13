package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "198")
public class KappaTechWrecker extends Card {

    public KappaTechWrecker() {
        addNinjutsu("{1}{G}");

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.DEATHTOUCH, new Fixed(1)));

        PermanentAllOfPredicate artifactOrEnchantmentControlledByDefendingPlayer = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate())),
                new PermanentControlledByDefendingPlayerPredicate()));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayEffect(
                new RemoveCounterFromSourceThenEffect(
                        CounterType.DEATHTOUCH,
                        new ExileTargetPermanentEffect(artifactOrEnchantmentControlledByDefendingPlayer)),
                "Remove a deathtouch counter from this creature?"));
    }
}
