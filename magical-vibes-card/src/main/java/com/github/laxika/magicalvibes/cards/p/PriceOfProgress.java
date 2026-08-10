package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "95")
public class PriceOfProgress extends Card {

    public PriceOfProgress() {
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(
                new Scaled(new PermanentCount(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsLandPredicate(),
                                        new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))
                                )),
                                CountScope.CONTROLLER
                        ), 2),
                DamageRecipient.EACH_PLAYER));
    }
}
