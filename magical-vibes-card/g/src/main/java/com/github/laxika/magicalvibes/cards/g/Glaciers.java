package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ICE", collectorNumber = "294")
public class Glaciers extends Card {

    public Glaciers() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {W}{U}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{W}{U}", new SacrificeSelfEffect()));

        // All Mountains are Plains.
        addStaticEffect(new GrantSubtypeEffect(CardSubtype.PLAINS, Subjects.allLands(), true,
                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)));
    }
}
