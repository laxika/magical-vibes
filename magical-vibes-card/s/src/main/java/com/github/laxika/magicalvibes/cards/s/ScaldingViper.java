package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "235")
@CardRegistration(set = "WOE", collectorNumber = "295")
public class ScaldingViper extends Card {

    public ScaldingViper() {
        setBackFaceCard(new SteamClean());
        addCastingOption(new AdventureCast("{1}{U}"));
        addTrigger(Triggers.opponentCastsSpell().withStackEntryFilter(new StackEntryMaxManaValuePredicate(3)),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER));
    }

    @Override
    public String getBackFaceClassName() {
        return "SteamClean";
    }
}
