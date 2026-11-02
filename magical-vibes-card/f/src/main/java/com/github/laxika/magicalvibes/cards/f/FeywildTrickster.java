package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "58")
public class FeywildTrickster extends Card {

    public FeywildTrickster() {
        addTrigger(Triggers.youRollOneOrMoreDice(),
                new CreateTokenEffect("Faerie Dragon", 1, 1, CardColor.BLUE,
                        List.of(CardSubtype.FAERIE, CardSubtype.DRAGON),
                        Set.of(Keyword.FLYING), Set.of()));
    }
}
