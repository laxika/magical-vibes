package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

/**
 * Elvish Spirit Guide — {2}{G} 2/2 Elf Spirit.
 *
 * <p>"Exile this creature from your hand: Add {G}." A mana ability activated from hand whose cost
 * exiles the card itself, so it resolves immediately without using the stack (CR 605.1a).</p>
 */
@CardRegistration(set = "ALL", collectorNumber = "89")
public class ElvishSpiritGuide extends Card {

    public ElvishSpiritGuide() {
        addHandActivatedAbility(new ActivatedAbility(false, null,
                List.of(new AwardManaEffect(ManaColor.GREEN)),
                "Exile this creature from your hand: Add {G}.")
                .withExilesSourceFromHand());
    }
}
