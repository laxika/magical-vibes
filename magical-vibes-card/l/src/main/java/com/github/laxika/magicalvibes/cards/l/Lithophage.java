package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "202")
@CardRegistration(set = "DDI", collectorNumber = "57")
public class Lithophage extends Card {

    public Lithophage() {
        // At the beginning of your upkeep, sacrifice this creature unless you sacrifice a Mountain.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new SacrificePermanentsEffect(1, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                        Subjects.you()),
                "Sacrifice a Mountain? If you don't, sacrifice this creature.", new SacrificeSelfEffect()));
    }
}
