package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "157")
public class ChalkOutline extends Card {

    public ChalkOutline() {
        addEffect(EffectSlot.ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD,
                SequenceEffect.of(
                        new CreateTokenEffect(1, "Detective", 2, 2, CardColor.WHITE,
                                Set.of(CardColor.WHITE, CardColor.BLUE), List.of(CardSubtype.DETECTIVE)),
                        CreateTokenEffect.ofClueToken(1)));
    }
}
