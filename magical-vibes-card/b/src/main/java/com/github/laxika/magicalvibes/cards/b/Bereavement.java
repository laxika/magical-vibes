package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "7ED", collectorNumber = "120")
@CardRegistration(set = "USG", collectorNumber = "117")
public class Bereavement extends Card {

    public Bereavement() {
        // Whenever a green creature dies, its controller discards a card.
        addTrigger(Triggers.creatureDies(new PermanentColorInPredicate(Set.of(CardColor.GREEN)), ObjectRelation.ANY),
                new DiscardEffect(1, Subjects.controllerOf(Subjects.triggering())));
    }
}
