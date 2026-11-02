package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsToken;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "92")
@CardRegistration(set = "2XM", collectorNumber = "212")
@CardRegistration(set = "GK2", collectorNumber = "123")
@CardRegistration(set = "PIO", collectorNumber = "238")
@CardRegistration(set = "C16", collectorNumber = "216")
public class ProgenitorMimic extends Card {

    public ProgenitorMimic() {
        // You may have this creature enter as a copy of any creature on the battlefield, except it
        // has "At the beginning of your upkeep, if this creature isn't a token, create a token
        // that's a copy of this creature."
        addStaticEffect(new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature",
                Set.of(),
                List.of(
                        new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), 
                        new ConditionalEffect(new NotCondition(new SourceIsToken()),
                                new CreateTokenCopyOfSourceEffect())))
        ));
    }
}
