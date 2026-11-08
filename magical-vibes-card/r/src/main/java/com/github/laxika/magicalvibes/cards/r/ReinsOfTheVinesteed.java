package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToSharedTypeCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOR", collectorNumber = "134")
public class ReinsOfTheVinesteed extends Card {

    public ReinsOfTheVinesteed() {
        target(TargetFilters.creature())
                // Enchanted creature gets +2/+2.
                .addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE))

                // When enchanted creature dies, you may return this card from your graveyard to the
                // battlefield attached to a creature that shares a creature type with that creature.
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new MayEffect(new ReturnSourceAuraToSharedTypeCreatureOnDeathEffect(),
                                "return it to the battlefield attached to a creature that shares a creature type"
                                        + " with the creature that died?"));
    }
}
