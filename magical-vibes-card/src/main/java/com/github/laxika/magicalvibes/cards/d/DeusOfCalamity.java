package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "SHM", collectorNumber = "204")
public class DeusOfCalamity extends Card {

    public DeusOfCalamity() {
        // Trample is auto-loaded from Scryfall.
        // Whenever this creature deals 6 or more damage to an opponent, destroy target land that player controls.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                new DestroyPermanentDamagedPlayerControlsEffect(new PermanentIsLandPredicate(), 6));
    }
}
