package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardHandThenDrawEffect;

@CardRegistration(set = "FIC", collectorNumber = "58")
@CardRegistration(set = "FIC", collectorNumber = "120")
public class Snort extends Card {

    public Snort() {
        addEffect(EffectSlot.SPELL, new EachPlayerMayDiscardHandThenDrawEffect(
                5, DealDamageToPlayersEffect.selectedOpponents(5)));
        addCastingOption(new FlashbackCast("{5}{R}"));
    }
}
