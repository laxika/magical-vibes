package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureSubtypeDeathsThisTurn;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "CHK", collectorNumber = "45")
public class SilentChantZubera extends Card {

    public SilentChantZubera() {
        addEffect(EffectSlot.ON_DEATH, new GainLifeEffect(new Scaled(
                new CreatureSubtypeDeathsThisTurn(CardSubtype.ZUBERA, CountScope.ANY_PLAYER), 2)));
    }
}
