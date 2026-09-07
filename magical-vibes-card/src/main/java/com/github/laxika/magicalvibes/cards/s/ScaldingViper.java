package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "235")
public class ScaldingViper extends Card {

    public ScaldingViper() {
        setBackFaceCard(new SteamClean());
        addCastingOption(new AdventureCast("{1}{U}"));
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER)),
                new StackEntryMaxManaValuePredicate(3)));
    }

    @Override
    public String getBackFaceClassName() {
        return "SteamClean";
    }
}
