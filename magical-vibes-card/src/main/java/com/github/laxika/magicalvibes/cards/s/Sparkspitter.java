package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "109")
public class Sparkspitter extends Card {

    public Sparkspitter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CreateTokenEffect(
                                CardType.CREATURE, 1, "Spark Elemental", 3, 1,
                                CardColor.RED, null, List.of(CardSubtype.ELEMENTAL),
                                Set.of(Keyword.TRAMPLE, Keyword.HASTE), Set.of(),
                                false, false,
                                Map.of(EffectSlot.END_STEP_TRIGGERED, new SacrificeSelfEffect()), List.of(),
                                false, false, false, 0, Set.of())
                ),
                "{R}, {T}, Discard a card: Create a 3/1 red Elemental creature token named Spark Elemental. "
                        + "It has trample, haste, and \"At the beginning of the end step, sacrifice this token.\""
        ));
    }
}
