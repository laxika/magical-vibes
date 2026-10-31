package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "ODY", collectorNumber = "194")
public class FlameBurst extends Card {

    public FlameBurst() {
        // 2 damage plus one for every card named Flame Burst in any graveyard. Pardic Firecat
        // counts as Flame Burst through its own graveyard static, so it is not named here.
        addSpellEffect(new DealDamageToAnyTargetEffect(new Sum(
                new Fixed(2),
                new CardsInGraveyard(new CardNamedPredicate("Flame Burst"), CountScope.ANY_PLAYER))));
    }
}
