package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventNoncombatDamageToControllerAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "37")
public class Purity extends Card {

    public Purity() {
        // Flying is auto-loaded from Scryfall.
        // "If noncombat damage would be dealt to you, prevent that damage. You gain life equal
        // to the damage prevented this way."
        addStaticEffect(new PreventNoncombatDamageToControllerAndGainLifeEffect());
        // "When Purity is put into a graveyard from anywhere, shuffle it into its owner's library."
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS), new ShuffleSelfFromGraveyardIntoLibraryEffect());
    }
}
