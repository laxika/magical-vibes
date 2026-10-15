package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "212")
public class InexorableBlob extends Card {

    public InexorableBlob() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new Delirium(), new CreateTokenEffect(
                        1, "Ooze", 3, 3, CardColor.GREEN, List.of(CardSubtype.OOZE), true)));
    }
}
