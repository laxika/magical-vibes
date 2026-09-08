package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "141")
public class CleanupCrew extends Card {

    public CleanupCrew() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact.",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsArtifactPredicate(), "Target must be an artifact.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target enchantment.",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsEnchantmentPredicate(), "Target must be an enchantment.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target card from a graveyard.",
                        new ExileTargetCardFromGraveyardAndGainLifeEffect(null, 0)),
                new ChooseOneEffect.ChooseOneOption("You gain 4 life.", new GainLifeEffect(4))
        ))));
    }
}
