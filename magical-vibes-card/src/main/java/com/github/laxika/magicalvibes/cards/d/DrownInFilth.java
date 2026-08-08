package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DGM", collectorNumber = "67")
public class DrownInFilth extends Card {

    public DrownInFilth() {
        // Choose target creature. Mill four cards, then that creature gets -1/-1 until end of
        // turn for each land card in your graveyard.
        addEffect(EffectSlot.SPELL, new MillEffect(4, MillRecipient.CONTROLLER));

        final Scaled minusOnePerLand = new Scaled(
                new CardsInGraveyard(new CardTypePredicate(CardType.LAND), CountScope.CONTROLLER), -1);
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(minusOnePerLand, minusOnePerLand));
    }
}
