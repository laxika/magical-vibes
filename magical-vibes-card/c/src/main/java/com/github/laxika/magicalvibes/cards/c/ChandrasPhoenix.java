package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "126")
@CardRegistration(set = "M14", collectorNumber = "134")
@CardRegistration(set = "E01", collectorNumber = "44")
public class ChandrasPhoenix extends Card {

    public ChandrasPhoenix() {
        // Whenever an opponent is dealt damage by a red instant or sorcery spell you control or by a
        // red planeswalker you control, return this card from your graveyard to your hand. The source
        // gating lives in the slot's collector; the effect is a mandatory self-return.
        addTrigger(new TriggerCondition(Triggers.opponentDealtDamageByRedInstantSorceryOrPlaneswalkerYouControl(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build());
    }
}
