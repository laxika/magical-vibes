package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WildwoodTracker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClockworkServant.class, WildwoodTracker.class})
class ClockworkServantTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when at least three mana of one color is spent")
    void drawsWhenThreeManaOfOneColorIsSpent() {
        harness.setHand(player1, List.of(new ClockworkServant()));
        harness.setLibrary(player1, List.of(new WildwoodTracker()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when the mana spent is split between colors")
    void doesNotDrawWhenManaIsSplitBetweenColors() {
        harness.setHand(player1, List.of(new ClockworkServant()));
        harness.setLibrary(player1, List.of(new WildwoodTracker()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when three colorless mana is spent")
    void doesNotDrawWhenThreeColorlessManaIsSpent() {
        harness.setHand(player1, List.of(new ClockworkServant()));
        harness.setLibrary(player1, List.of(new WildwoodTracker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when fewer than three mana of any color was spent")
    void doesNotTriggerWhenManaIsSplitBetweenColors() {
        harness.setHand(player1, List.of(new ClockworkServant()));
        harness.setLibrary(player1, List.of(new WildwoodTracker()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Clockwork Servant");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast does not trigger adamant")
    void doesNotTriggerWhenPutOntoBattlefield() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new WildwoodTracker()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.enterBattlefieldAndReturn(player1, new ClockworkServant());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "GREEN"})
    @DisplayName("Adamant accepts each other color of mana")
    void drawsForEachOtherColor(ManaColor color) {
        harness.setHand(player1, List.of(new ClockworkServant()));
        harness.setLibrary(player1, List.of(new WildwoodTracker()));
        harness.addMana(player1, color, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

}
