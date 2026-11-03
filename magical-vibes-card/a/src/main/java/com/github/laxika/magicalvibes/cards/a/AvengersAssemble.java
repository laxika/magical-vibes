package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AnotherPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesOfSubtypeThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "6")
public class AvengersAssemble extends Card {

    public AvengersAssemble() {
        addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.HERO)));
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AnyOf(List.of(
                                new AttackedWithCreaturesOfSubtypeThisTurn(1, CardSubtype.HERO),
                                new AnotherPermanentEnteredThisTurn(new CardSubtypePredicate(CardSubtype.HERO))))),
                new DrawCardEffect(1));
    }
}
