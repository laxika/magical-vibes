package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AA2", collectorNumber = "22")
public class TriplicateTitan extends Card {

    public TriplicateTitan() {
        // When this creature dies, create a 3/3 colorless Golem artifact creature token with flying,
        // a 3/3 colorless Golem artifact creature token with vigilance, and a 3/3 colorless Golem
        // artifact creature token with trample.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Golem", 3, 3, null,
                List.of(CardSubtype.GOLEM), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT)));
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Golem", 3, 3, null,
                List.of(CardSubtype.GOLEM), Set.of(Keyword.VIGILANCE), Set.of(CardType.ARTIFACT)));
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Golem", 3, 3, null,
                List.of(CardSubtype.GOLEM), Set.of(Keyword.TRAMPLE), Set.of(CardType.ARTIFACT)));
    }
}
