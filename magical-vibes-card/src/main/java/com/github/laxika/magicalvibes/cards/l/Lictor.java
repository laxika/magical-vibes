package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.OpponentPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "94")
public class Lictor extends Card {

    public Lictor() {
        // Pheromone Trail — when this creature enters, create a Tyranid Warrior token if a
        // creature entered the battlefield under an opponent's control this turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new OpponentPermanentEnteredThisTurn(new CardTypePredicate(CardType.CREATURE), 1),
                new CreateTokenEffect(
                        "Tyranid Warrior", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.TYRANID, CardSubtype.WARRIOR),
                        Set.of(Keyword.TRAMPLE), Set.of())));
    }
}
