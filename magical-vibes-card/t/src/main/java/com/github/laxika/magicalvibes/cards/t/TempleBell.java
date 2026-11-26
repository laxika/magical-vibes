package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "217")
@CardRegistration(set = "C13", collectorNumber = "265")
@CardRegistration(set = "C16", collectorNumber = "277")
@CardRegistration(set = "LCC", collectorNumber = "117")
public class TempleBell extends Card {

    public TempleBell() {
        // {T}: Each player draws a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect(1, Subjects.eachPlayer())),
                "{T}: Each player draws a card."
        ));
    }
}
