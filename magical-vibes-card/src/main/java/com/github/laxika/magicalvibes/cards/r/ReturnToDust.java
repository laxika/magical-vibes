package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerMainPhase;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "39")
@CardRegistration(set = "TSR", collectorNumber = "37")
@CardRegistration(set = "CMD", collectorNumber = "28")
@CardRegistration(set = "SLZ", collectorNumber = "8")
@CardRegistration(set = "SLZ", collectorNumber = "129")
@CardRegistration(set = "SLZ", collectorNumber = "250")
@CardRegistration(set = "C14", collectorNumber = "85")
@CardRegistration(set = "CMM", collectorNumber = "52")
@CardRegistration(set = "CMM", collectorNumber = "628")
@CardRegistration(set = "WHO", collectorNumber = "211")
@CardRegistration(set = "WHO", collectorNumber = "802")
@CardRegistration(set = "MOC", collectorNumber = "202")
@CardRegistration(set = "C21", collectorNumber = "100")
@CardRegistration(set = "DSC", collectorNumber = "102")
@CardRegistration(set = "LCC", collectorNumber = "136")
@CardRegistration(set = "MIC", collectorNumber = "92")
@CardRegistration(set = "C18", collectorNumber = "71")
@CardRegistration(set = "KHC", collectorNumber = "32")
@CardRegistration(set = "C17", collectorNumber = "70")
public class ReturnToDust extends Card {

    public ReturnToDust() {
        PermanentPredicateTargetFilter artifactOrEnchantment = new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                "Target must be an artifact or enchantment"
        );

        target(artifactOrEnchantment)
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
        target(artifactOrEnchantment, 0, 1)
                .addEffect(EffectSlot.SPELL,
                        new ConditionalEffect(new ControllerMainPhase(), new ExileTargetPermanentEffect()));
    }
}
