package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "194")
public class FlameBurst extends Card {

    public FlameBurst() {
        // 2 damage plus one for every card named Flame Burst in any graveyard. Pardic Firecat
        // counts as Flame Burst for this effect while it is in a graveyard.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new Sum(
                new Fixed(2),
                new CardsInGraveyard(new CardAnyOfPredicate(List.of(
                        new CardNamedPredicate("Flame Burst"),
                        new CardNamedPredicate("Pardic Firecat"))), CountScope.ANY_PLAYER))));
    }
}
