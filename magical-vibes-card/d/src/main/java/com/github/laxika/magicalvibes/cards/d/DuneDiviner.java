package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "114")
public class DuneDiviner extends Card {

    public DuneDiviner() {
        // {1}, Tap an untapped Desert you control: You gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.DESERT)),
                        new GainLifeEffect(1)),
                "{1}, Tap an untapped Desert you control: You gain 1 life."));
    }
}
