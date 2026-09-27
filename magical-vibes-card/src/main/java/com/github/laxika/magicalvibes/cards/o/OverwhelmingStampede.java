package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "M11", collectorNumber = "189")
@CardRegistration(set = "MM2", collectorNumber = "153")
@CardRegistration(set = "MSC", collectorNumber = "178")
@CardRegistration(set = "MSC", collectorNumber = "385")
@CardRegistration(set = "C14", collectorNumber = "208")
@CardRegistration(set = "C15", collectorNumber = "196")
@CardRegistration(set = "LTC", collectorNumber = "254")
public class OverwhelmingStampede extends Card {

    public OverwhelmingStampede() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(
                new GreatestPowerAmongControlled(), new GreatestPowerAmongControlled()));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
    }
}
