package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.SpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "FUT", collectorNumber = "122")
@CardRegistration(set = "TSR", collectorNumber = "193")
@CardRegistration(set = "DMR", collectorNumber = "142")
public class StormEntity extends Card {

    public StormEntity() {
        var otherSpellsCastThisTurn = new SpellsCastThisTurn(
                new CardNotPredicate(new CardIsSelfPredicate()), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, otherSpellsCastThisTurn));
    }
}
