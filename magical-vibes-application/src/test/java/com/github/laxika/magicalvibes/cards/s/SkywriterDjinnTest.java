package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkywriterDjinn.class, Plains.class, Island.class, Forest.class})
class SkywriterDjinnTest extends BaseCardTest {

    private static final Set<String> SPELLBOOK = Set.of(
            "See the Truth", "Teferi's Time Twist", "Flood of Recollection", "Keep Safe",
            "Hard Evidence", "Ghostform", "Startle", "Hampering Snare", "Stifle",
            "Contentious Plan", "Majestic Metamorphosis", "Befuddle", "Bury in Books",
            "Choking Tethers", "Suit Up");

    @Test
    @DisplayName("ETB conjures one spellbook card without Domain")
    void etbConjuresOneCard() {
        harness.setHand(player1, List.of(new SkywriterDjinn()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .hasSize(1)
                .allMatch(Card::isTokenCard)
                .extracting(Card::getName)
                .allMatch(SPELLBOOK::contains);
    }

    @Test
    @DisplayName("Domain repeats until hand size reaches the number of basic land types")
    void domainRepeatsForBasicLandTypes() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new SkywriterDjinn()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .hasSize(3)
                .allMatch(Card::isTokenCard)
                .extracting(Card::getName)
                .allMatch(SPELLBOOK::contains);
    }
}
