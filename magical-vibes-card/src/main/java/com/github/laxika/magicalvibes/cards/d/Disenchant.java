package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "22")
@CardRegistration(set = "5ED", collectorNumber = "26")
@CardRegistration(set = "7ED", collectorNumber = "13")
@CardRegistration(set = "6ED", collectorNumber = "16")
@CardRegistration(set = "ICE", collectorNumber = "20")
@CardRegistration(set = "MIR", collectorNumber = "10")
@CardRegistration(set = "TMP", collectorNumber = "16")
@CardRegistration(set = "USG", collectorNumber = "12")
@CardRegistration(set = "M20", collectorNumber = "14")
@CardRegistration(set = "TPR", collectorNumber = "11")
@CardRegistration(set = "ATH", collectorNumber = "5")
@CardRegistration(set = "BRO", collectorNumber = "6")
@CardRegistration(set = "MMQ", collectorNumber = "18")
@CardRegistration(set = "BRB", collectorNumber = "20")
@CardRegistration(set = "SUM", collectorNumber = "17")
public class Disenchant extends Card {

    public Disenchant() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                "Target must be an artifact or enchantment"
        ))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
