package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "14")
@CardRegistration(set = "MSC", collectorNumber = "304")
public class FalconAndRedwing extends Card {

    public FalconAndRedwing() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, SequenceEffect.of(
                new CreateTokenEffect(
                        new EventValue(), "Bird", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()),
                new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
