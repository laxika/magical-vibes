package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventNoncombatDamageToControllerAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "LRW", collectorNumber = "37")
public class Purity extends Card {

    public Purity() {
        // Flying is auto-loaded from Scryfall.
        // "If noncombat damage would be dealt to you, prevent that damage. You gain life equal
        // to the damage prevented this way."
        addEffect(EffectSlot.STATIC, new PreventNoncombatDamageToControllerAndGainLifeEffect());
        // "When Purity is put into a graveyard from anywhere, shuffle it into its owner's library."
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, new ShuffleSelfFromGraveyardIntoLibraryEffect());
    }
}
