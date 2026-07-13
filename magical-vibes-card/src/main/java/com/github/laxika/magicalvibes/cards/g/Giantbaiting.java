package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "207")
public class Giantbaiting extends Card {

    public Giantbaiting() {
        // Create a 4/4 red and green Giant Warrior creature token with haste.
        // Exile it at the beginning of the next end step.
        // (Conspire is handled entirely by the casting flow via the Scryfall keyword.)
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE,
                new Fixed(1),
                "Giant Warrior",
                4,
                4,
                CardColor.RED,
                Set.of(CardColor.RED, CardColor.GREEN),
                List.of(CardSubtype.GIANT, CardSubtype.WARRIOR),
                Set.of(Keyword.HASTE),
                Set.of(),
                false,  // tappedAndAttacking
                false,  // tapped
                Map.of(),
                List.of(),
                false,  // exileAtEndOfCombat
                true,   // exileAtEndStep
                false,  // legendary
                0,
                Set.of()
        ));
    }
}
