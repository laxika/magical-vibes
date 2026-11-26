package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachCreatedEquipmentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndSacrificeAttachedEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "22")
public class OutfittedJouster extends Card {

    public OutfittedJouster() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                ConjureCardsEffect.eachOf(List.of(
                        new DraftFromSpellbookEffect.SpellbookCard("ELD", "202"),
                        new DraftFromSpellbookEffect.SpellbookCard("M15", "213")), ConjureDestination.ontoBattlefield()),
                new AttachCreatedEquipmentToSourceEffect()));

        addStaticEffect(new PreventDamageAndSacrificeAttachedEquipmentEffect());
    }
}
