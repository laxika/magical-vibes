package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageLootEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "60")
public class JaceCunningCastaway extends Card {

    public JaceCunningCastaway() {
        // +1: Whenever one or more creatures you control deal combat damage to a player this turn,
        //     draw a card, then discard a card.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new RegisterDelayedCombatDamageLootEffect(1, 1)),
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
                        Map.of(EffectSlot.ON_BECOMES_TARGET_OF_SPELL, new SacrificeSelfEffect())
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
