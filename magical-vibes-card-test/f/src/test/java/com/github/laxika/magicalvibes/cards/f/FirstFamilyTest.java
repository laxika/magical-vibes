package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.w.WordsOfWind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirstFamily.class, GiantGrowth.class, GrizzlyBears.class, HolyDay.class, Island.class,
        SavannahLions.class, WordsOfWind.class})
class FirstFamilyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws and gains life for the distinct colors among permanents and spells")
    void drawsAndGainsLifeForDistinctColors() {
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SavannahLions());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new GiantGrowth(), new FirstFamily()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Counts battlefield colors present before resolution")
    void countsColorsAtResolution() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new FirstFamily()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.addToBattlefield(player1, new SavannahLions());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Counts its own colors but not an opponent's permanents or spells")
    void countsItselfAndIgnoresOpponentColors() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new SavannahLions());
        harness.setHand(player2, List.of(new HolyDay()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0);

        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new FirstFamily()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Includes a resolved spell's color even without a permanent of that color")
    void countsResolvedSpells() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new HolyDay(), new FirstFamily()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Uses the same X for drawing and life gain when a draw replacement removes a color")
    void keepsXWhenDrawReplacementReturnsOnlyWhitePermanent() {
        harness.addToBattlefield(player1, new WordsOfWind());
        var lions = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new FirstFamily()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(lions.getId()));

        harness.assertNotOnBattlefield(player1, "Savannah Lions");
        assertThat(gd.playerHands.get(player1.getId())).contains(lions.getCard()).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 23);
    }
}
