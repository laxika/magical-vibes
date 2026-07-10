package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToOtherCreaturesAndAddPlusCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "LRW", collectorNumber = "240")
public class Vigor extends Card {

    public Vigor() {
        // Trample is auto-loaded from Scryfall.
        // "If damage would be dealt to another creature you control, prevent that damage.
        // Put a +1/+1 counter on that creature for each 1 damage prevented this way."
        addEffect(EffectSlot.STATIC, new PreventDamageToOtherCreaturesAndAddPlusCountersEffect());
        // "When Vigor is put into a graveyard from anywhere, shuffle it into its owner's library."
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, new ShuffleSelfFromGraveyardIntoLibraryEffect());
    }
}
