package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SOS", collectorNumber = "234")
public class StirringHonormancer extends Card {

    public StirringHonormancer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LookAtTopCardsEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                new Fixed(1), null, LookDestination.GRAVEYARD, false));
    }
}
