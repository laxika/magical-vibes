package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "97")
@CardRegistration(set = "LEG", collectorNumber = "71")
public class RelicBind extends Card {

    public RelicBind() {
        // Enchant artifact an opponent controls.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Target must be an artifact an opponent controls"
        ));
        // Whenever enchanted artifact becomes tapped, choose one —
        //  • This Aura deals 1 damage to target player or planeswalker.
        //  • Target player gains 1 life.
        addTrigger(Triggers.enchantedPermanentBecomesTapped(), new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("This Aura deals 1 damage to target player or planeswalker",
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Target player gains 1 life",
                        new TargetPlayerGainsLifeEffect(1)))));
    }
}
