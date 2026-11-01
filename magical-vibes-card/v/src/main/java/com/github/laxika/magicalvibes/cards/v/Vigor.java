package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToOtherCreaturesAndAddPlusCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "240")
@CardRegistration(set = "DPA", collectorNumber = "88")
@CardRegistration(set = "TMC", collectorNumber = "53")
@CardRegistration(set = "MB1", collectorNumber = "215")
@CardRegistration(set = "PIP", collectorNumber = "347")
@CardRegistration(set = "PIP", collectorNumber = "875")
public class Vigor extends Card {

    public Vigor() {
        // Trample is auto-loaded from Scryfall.
        // "If damage would be dealt to another creature you control, prevent that damage.
        // Put a +1/+1 counter on that creature for each 1 damage prevented this way."
        addStaticEffect(new PreventDamageToOtherCreaturesAndAddPlusCountersEffect());
        // "When Vigor is put into a graveyard from anywhere, shuffle it into its owner's library."
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS), new ShuffleSelfFromGraveyardIntoLibraryEffect());
    }
}
