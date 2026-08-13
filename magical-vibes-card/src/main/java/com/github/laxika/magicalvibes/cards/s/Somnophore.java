package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "USG", collectorNumber = "97")
public class Somnophore extends Card {

    public Somnophore() {
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                TapChosenPermanentEffect.damagedPlayerControls(new PermanentIsCreaturePredicate()));
    }
}
