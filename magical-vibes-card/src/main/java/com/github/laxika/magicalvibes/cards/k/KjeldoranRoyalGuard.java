package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectUnblockedCombatDamageToSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "25")
public class KjeldoranRoyalGuard extends Card {

    public KjeldoranRoyalGuard() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new RedirectUnblockedCombatDamageToSelfEffect()), false, "{T}: All combat damage that would be dealt to you by unblocked creatures this turn is dealt to Kjeldoran Royal Guard instead."));
    }
}
