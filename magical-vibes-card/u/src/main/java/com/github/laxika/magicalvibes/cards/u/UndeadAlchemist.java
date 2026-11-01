package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileMilledCreatureAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceCombatDamageWithMillEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "84")
@CardRegistration(set = "SLD", collectorNumber = "836")
@CardRegistration(set = "MIC", collectorNumber = "105")
public class UndeadAlchemist extends Card {

    public UndeadAlchemist() {
        // If a Zombie you control would deal combat damage to a player, instead that
        // player mills that many cards.
        addStaticEffect(new ReplaceCombatDamageWithMillEffect(
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)
        ));

        // Whenever a creature card is put into an opponent's graveyard from their library,
        // exile that card and create a 2/2 black Zombie creature token.
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE,
                ObjectRelation.OPPONENT_OWNS),
                new ExileMilledCreatureAndCreateTokenEffect(
                "Zombie", 2, 2, CardColor.BLACK, List.of(CardSubtype.ZOMBIE)
        ));
    }
}
