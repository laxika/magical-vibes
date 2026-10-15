package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyDamageSourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "LRW", collectorNumber = "107")
public class Dread extends Card {

    public Dread() {
        // "Whenever a creature deals damage to you, destroy it." (Fear is auto-loaded metadata.)
        addEffect(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU,
                new DestroyDamageSourcePermanentEffect(new PermanentIsCreaturePredicate()));
        // "When Dread is put into a graveyard from anywhere, shuffle it into its owner's library."
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new ShuffleSelfFromGraveyardIntoLibraryEffect());
    }
}
