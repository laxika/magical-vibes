package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "40")
public class OscorpResearchTeam extends Card {

    public OscorpResearchTeam() {
        addActivatedAbility(new ActivatedAbility(false, "{6}{U}", List.of(new DrawCardEffect(2)), "{6}{U}: Draw two cards."));
    }
}
