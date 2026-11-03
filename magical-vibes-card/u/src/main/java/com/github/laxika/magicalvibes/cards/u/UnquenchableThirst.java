package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "53")
@CardRegistration(set = "AKR", collectorNumber = "86")
public class UnquenchableThirst extends Card {

    public UnquenchableThirst() {
        // Enchant creature
        target(TargetFilters.creature())
                // When this Aura enters, if you control a Desert or there is a Desert card in
                // your graveyard, tap enchanted creature.
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        TriggerOptions.DEFAULT.withInterveningIf(
                                new AnyOf(List.of(
                                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DESERT)),
                                        new GraveyardCardThreshold(1, new CardSubtypePredicate(CardSubtype.DESERT))
                                ))),
                        new TapPermanentsEffect(TapUntapScope.ENCHANTED));

        // Enchanted creature doesn't untap during its controller's untap step.
        addStaticEffect(DoesntUntapEffect.enchanted());
    }
}
