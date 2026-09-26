package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileXCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayCastMilledSpellEffect;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "70")
@CardRegistration(set = "LTC", collectorNumber = "150")
public class SummonsOfSaruman extends Card {

    public SummonsOfSaruman() {
        addEffect(EffectSlot.SPELL, new AmassGoblinsEffect(new XValue(), CardSubtype.ORC));
        addEffect(EffectSlot.SPELL, new MillControllerAndMayCastMilledSpellEffect(new XValue()));
        addCastingOption(new FlashbackCast(List.of(
                new ManaCastingCost("{3}{U}{R}"),
                new ExileXCardsFromGraveyardCastingCost(null, null))));
    }
}
