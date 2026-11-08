package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "107")
public class Dread extends Card {

    public Dread() {
        // "Whenever a creature deals damage to you, destroy it." (Fear is auto-loaded metadata.)
        addTrigger(Triggers.permanentDealsDamageToYou().withFilter(new PermanentIsCreaturePredicate()),
                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));
        // "When Dread is put into a graveyard from anywhere, shuffle it into its owner's library."
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS),
                new ShuffleSelfFromGraveyardIntoLibraryEffect());
    }
}
