package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileMilledCreatureAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceCombatDamageWithMillEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "84")
public class UndeadAlchemist extends Card {

    public UndeadAlchemist() {
        // If a Zombie you control would deal combat damage to a player, instead that
        // player mills that many cards.
        addEffect(EffectSlot.STATIC, new ReplaceCombatDamageWithMillEffect(
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)
        ));

        // Whenever a creature card is put into an opponent's graveyard from their library,
        // exile that card and create a 2/2 black Zombie creature token.
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_CARD_MILLED, new ExileMilledCreatureAndCreateTokenEffect(
                "Zombie", 2, 2, CardColor.BLACK, List.of(CardSubtype.ZOMBIE)
        ));
    }
}
