package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "XLN", collectorNumber = "60")
public class JaceCunningCastaway extends Card {

    public JaceCunningCastaway() {
        // +1: Whenever one or more creatures you control deal combat damage to a player this turn,
        //     draw a card, then discard a card.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(new TriggeredAbility(
                        Triggers.creatureYouControlDealsCombatDamageToPlayer(), TriggerOptions.ONE_OR_MORE,
                        new DrawCardEffect(1), new DiscardEffect(1, Subjects.you())))),
                "+1: Whenever one or more creatures you control deal combat damage to a player this turn, draw a card, then discard a card."
        ));

        // −2: Create a 2/2 blue Illusion creature token with
        //     "When this creature becomes the target of a spell, sacrifice it."
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        1, "Illusion", 2, 2,
                        CardColor.BLUE, List.of(CardSubtype.ILLUSION),
                        Set.of(), Set.of(),
                        List.of(new TriggeredAbility(Triggers.becomesTargetOfSpell(), new SacrificeSelfEffect()))
                )),
                "\u22122: Create a 2/2 blue Illusion creature token with \"When this creature becomes the target of a spell, sacrifice it.\""
        ));

        // −5: Create two tokens that are copies of Jace, Cunning Castaway, except they're not legendary.
        addActivatedAbility(new ActivatedAbility(
                -5,
                List.of(new CreateTokenCopyOfSourceEffect(true, 2)),
                "\u22125: Create two tokens that are copies of Jace, Cunning Castaway, except they're not legendary."
        ));
    }
}
