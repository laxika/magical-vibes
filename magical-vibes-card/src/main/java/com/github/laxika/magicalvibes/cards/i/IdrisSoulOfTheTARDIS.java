package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ImprintedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAndTriggeredAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "135")
@CardRegistration(set = "WHO", collectorNumber = "419")
@CardRegistration(set = "WHO", collectorNumber = "740")
@CardRegistration(set = "WHO", collectorNumber = "1010")
public class IdrisSoulOfTheTARDIS extends Card {

    public IdrisSoulOfTheTARDIS() {
        PermanentPredicate artifactYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(), "Target must be an artifact you control"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentUntilSourceLeavesEffect(true, artifactYouControl));
        addEffect(EffectSlot.STATIC, new GainActivatedAndTriggeredAbilitiesOfExiledCardsEffect());
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new ImprintedCardManaValue(), new ImprintedCardManaValue()));
    }
}
