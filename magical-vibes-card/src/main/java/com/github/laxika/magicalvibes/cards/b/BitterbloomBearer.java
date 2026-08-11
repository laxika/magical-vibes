package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "88")
public class BitterbloomBearer extends Card {

    public BitterbloomBearer() {
        // At the beginning of your upkeep, you lose 1 life and create a 1/1 blue and black Faerie creature token with flying.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new LoseLifeEffect(1),
                new CreateTokenEffect(1, "Faerie", 1, 1, CardColor.BLUE,
                        Set.of(CardColor.BLUE, CardColor.BLACK),
                        List.of(CardSubtype.FAERIE), Set.of(Keyword.FLYING), Set.of())));
    }
}
