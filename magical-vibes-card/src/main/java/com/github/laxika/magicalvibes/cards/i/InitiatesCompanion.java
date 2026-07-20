package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.UntapChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "174")
public class InitiatesCompanion extends Card {

    public InitiatesCompanion() {
        // Whenever this creature deals combat damage to a player, untap target creature or land.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new UntapChosenPermanentEffect(new PermanentAnyOfPredicate(
                        List.of(new PermanentIsCreaturePredicate(), new PermanentIsLandPredicate()))));
    }
}
