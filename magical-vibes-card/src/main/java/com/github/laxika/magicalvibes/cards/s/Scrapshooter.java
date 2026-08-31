package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "191")
public class Scrapshooter extends Card {

    public Scrapshooter() {
        addEffect(EffectSlot.STATIC, new GiftEffect(0));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new GiftPromised(), new EachOtherPlayerDrawsCardEffect(1)));

        PermanentPredicateTargetFilter targetArtifactOrEnchantmentAnOpponentControls =
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsEnchantmentPredicate()
                                )),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                        )),
                        "Target must be an artifact or enchantment an opponent controls");
        targetWhenGiftPromised(targetArtifactOrEnchantmentAnOpponentControls, 0, 1, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ConditionalEffect(new GiftPromised(), new DestroyTargetPermanentEffect()));
    }
}
