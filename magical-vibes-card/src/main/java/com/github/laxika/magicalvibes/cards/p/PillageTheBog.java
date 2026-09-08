package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "OTJ", collectorNumber = "224")
public class PillageTheBog extends Card {

    public PillageTheBog() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(
                new Scaled(new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER), 2), 1));
    }
}
