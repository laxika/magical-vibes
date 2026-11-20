package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ALA", collectorNumber = "78")
public class GrixisBattlemage extends Card {

    public GrixisBattlemage() {
        // {U}, {T}: Draw a card, then discard a card.
        addActivatedAbility(new ActivatedAbility(true, "{U}",
                List.of(new DrawCardEffect(1), new DiscardEffect(1, Subjects.you())),
                "{U}, {T}: Draw a card, then discard a card."));

        // {R}, {T}: Target creature can't block this turn.
        addActivatedAbility(new ActivatedAbility(true, "{R}",
                List.of(new CantBlockThisTurnEffect(Subjects.target())),
                "{R}, {T}: Target creature can't block this turn.",
                TargetFilters.creature()));
    }
}
