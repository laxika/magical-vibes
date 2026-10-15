package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeThisTurnEffect;

@CardRegistration(set = "MOM", collectorNumber = "52")
@CardRegistration(set = "MOM", collectorNumber = "351")
public class CompleteTheCircuit extends Card {

    public CompleteTheCircuit() {
        addEffect(EffectSlot.SPELL, new GrantFlashToCardTypeThisTurnEffect(CardType.SORCERY));
        addEffect(EffectSlot.SPELL, new CopyNextInstantOrSorceryCastThisTurnEffect(new Fixed(2)));
    }
}
