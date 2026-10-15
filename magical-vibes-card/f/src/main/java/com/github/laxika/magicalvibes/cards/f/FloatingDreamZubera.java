package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureSubtypeDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "CHK", collectorNumber = "61")
public class FloatingDreamZubera extends Card {

    public FloatingDreamZubera() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect(
                new CreatureSubtypeDeathsThisTurn(CardSubtype.ZUBERA, CountScope.ANY_PLAYER)));
    }
}
