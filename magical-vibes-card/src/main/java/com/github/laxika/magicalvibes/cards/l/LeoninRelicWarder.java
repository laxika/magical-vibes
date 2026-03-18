package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "10")
public class LeoninRelicWarder extends Card {

    public LeoninRelicWarder() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                "Target must be an artifact or enchantment"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(new ExileTargetPermanentUntilSourceLeavesEffect(), "Exile target artifact or enchantment?"));
    }
}
