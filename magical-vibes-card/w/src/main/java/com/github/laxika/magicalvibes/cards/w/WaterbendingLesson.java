package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TLA", collectorNumber = "80")
public class WaterbendingLesson extends Card {

    public WaterbendingLesson() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new ForcedCostOrElseEffect(new WaterbendCost(2),
                List.of(new DiscardEffect(1, Subjects.you())), true));
    }
}
