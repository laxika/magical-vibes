package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsFaceDown;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "MKM", collectorNumber = "203")
public class FaerieSnoop extends Card {

    public FaerieSnoop() {
        addMorph("{1}{U/B}{U/B}");
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new ConditionalEffect(new SourceIsFaceDown(), new CounterUnlessPaysEffect(2)));
        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(2, 1));
    }
}
