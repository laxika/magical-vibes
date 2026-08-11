package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "THS", collectorNumber = "199")
public class ProphetOfKruphix extends Card {

    public ProphetOfKruphix() {
        addEffect(EffectSlot.STATIC,
                new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep.UNTAP));
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(
                new CardTypePredicate(CardType.CREATURE)));
    }
}
