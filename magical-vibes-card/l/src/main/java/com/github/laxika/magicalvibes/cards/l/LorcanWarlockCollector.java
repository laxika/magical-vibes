package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.TriggeringCardManaValue;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCreaturesOfSubtypeInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "AFC", collectorNumber = "27")
public class LorcanWarlockCollector extends Card {

    public LorcanWarlockCollector() {
        // Whenever a creature card is put into an opponent's graveyard from anywhere, you may pay
        // life equal to its mana value. If you do, put it onto the battlefield under your control.
        // It's a Warlock in addition to its other types.
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.OPPONENT_OWNS),
                UnlessPaysEffect.youMayPayLife(new TriggeringCardManaValue(), new PutCardsOntoBattlefieldEffect(Subjects.triggeringCards())
                        .underControlOf(Subjects.you())
                        .enteringWith(new GrantSubtypeEffect(CardSubtype.WARLOCK, Subjects.target())))
                        .withPrompt("Pay life equal to that card's mana value to return it to the battlefield?"));

        // If a Warlock you control would die, exile it instead.
        addStaticEffect(new ExileOwnCreaturesOfSubtypeInsteadOfDyingEffect(CardSubtype.WARLOCK));
    }
}
