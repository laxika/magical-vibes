package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "HOC", collectorNumber = "104")
public class DInOfTheAncientHalls extends Card {

    public DInOfTheAncientHalls() {
        addEffect(EffectSlot.ON_ATTACK, new DealDamageToPlayersEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.DWARF), CountScope.CONTROLLER),
                DamageRecipient.EACH_OPPONENT));
    }
}
