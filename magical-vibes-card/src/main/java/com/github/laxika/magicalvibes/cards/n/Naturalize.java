package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "282")
@CardRegistration(set = "M10", collectorNumber = "195")
@CardRegistration(set = "M11", collectorNumber = "186")
@CardRegistration(set = "M12", collectorNumber = "185")
@CardRegistration(set = "M13", collectorNumber = "178")
@CardRegistration(set = "M14", collectorNumber = "186")
@CardRegistration(set = "M15", collectorNumber = "185")
@CardRegistration(set = "ISD", collectorNumber = "197")
@CardRegistration(set = "9ED", collectorNumber = "258")
@CardRegistration(set = "8ED", collectorNumber = "270")
@CardRegistration(set = "ALA", collectorNumber = "141")
@CardRegistration(set = "GTC", collectorNumber = "127")
@CardRegistration(set = "M19", collectorNumber = "190")
@CardRegistration(set = "KTK", collectorNumber = "142")
@CardRegistration(set = "ROE", collectorNumber = "199")
@CardRegistration(set = "RIX", collectorNumber = "139")
@CardRegistration(set = "ONS", collectorNumber = "275")
public class Naturalize extends Card {

    public Naturalize() {
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
