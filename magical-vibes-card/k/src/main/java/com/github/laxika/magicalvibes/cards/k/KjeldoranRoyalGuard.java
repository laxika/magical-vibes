package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "44")
@CardRegistration(set = "10E", collectorNumber = "25")
@CardRegistration(set = "7ED", collectorNumber = "23")
@CardRegistration(set = "6ED", collectorNumber = "28")
@CardRegistration(set = "ICE", collectorNumber = "38")
public class KjeldoranRoyalGuard extends Card {

    public KjeldoranRoyalGuard() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(GrantStaticEffectToPlayerEffect.untilEndOfTurn(
                RedirectPlayerDamageToSelfEffect.combatDamageFromUnblockedCreatures(false))), "{T}: All combat damage that would be dealt to you by unblocked creatures this turn is dealt to Kjeldoran Royal Guard instead."));
    }
}
