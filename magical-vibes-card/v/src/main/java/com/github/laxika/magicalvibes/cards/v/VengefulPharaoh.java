package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "116")
public class VengefulPharaoh extends Card {

    public VengefulPharaoh() {
        // Whenever combat damage is dealt to you or a planeswalker you control, if this card is in
        // your graveyard, destroy target attacking creature, then put this card on top of your
        // library. The intervening-if is re-checked on resolution (CR 603.4) for both halves, so the
        // ability does nothing if the card left the graveyard after the trigger, even if it has since
        // returned there as a new object (CR 400.7).
        target(TargetFilters.attackingCreature())
                .addTrigger(new TriggerCondition(Triggers.youOrYourPlaneswalkerDealtCombatDamage(),
                                AbilityZones.GRAVEYARD),
                        TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard()),
                        new DestroyTargetPermanentEffect());
        addTrigger(new TriggerCondition(Triggers.youOrYourPlaneswalkerDealtCombatDamage(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard()),
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0));
    }
}
