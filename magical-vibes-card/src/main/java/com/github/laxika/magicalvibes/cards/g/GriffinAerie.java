package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "22")
public class GriffinAerie extends Card {

    public GriffinAerie() {
        // At the beginning of your end step, if you gained 3 or more life this turn,
        // create a 2/2 white Griffin creature token with flying.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(3),
                new CreateTokenEffect("Griffin", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.GRIFFIN), Set.of(Keyword.FLYING), Set.of())));
    }
}
