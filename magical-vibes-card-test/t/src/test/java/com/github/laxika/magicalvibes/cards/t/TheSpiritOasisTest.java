package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.cards.s.SouthernAirTemple;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSpiritOasis.class, HondenOfSeeingWinds.class, Forest.class, GloriousAnthem.class,
        SouthernAirTemple.class})
class TheSpiritOasisTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself once and excludes opposing Shrines on entry")
    void countsItselfOnceAndExcludesOpposingShrines() {
        harness.addToBattlefield(player2, new SouthernAirTemple());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new TheSpiritOasis());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counts Shrines at resolution and separately draws for a later Shrine entering")
    void countsShrinesAtResolution() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new TheSpiritOasis());
        harness.enterBattlefieldAndReturn(player1, new SouthernAirTemple());
        for (int attempts = 0; attempts < 10 && !gd.stack.isEmpty(); attempts++) {
            harness.passBothPriorities();
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Draws for each Shrine controlled when it enters")
    void drawsForEachControlledShrineOnEntry() {
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TheSpiritOasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Draws when another Shrine enters under its controller's control")
    void drawsWhenAnotherShrineEnters() {
        harness.addToBattlefield(player1, new TheSpiritOasis());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.enterBattlefieldAndReturn(player1, new HondenOfSeeingWinds());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not draw when a non-Shrine enchantment or an opponent's Shrine enters")
    void ignoresNonShrinesAndOpponentsShrines() {
        harness.addToBattlefield(player1, new TheSpiritOasis());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.enterBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player2, new HondenOfSeeingWinds());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
