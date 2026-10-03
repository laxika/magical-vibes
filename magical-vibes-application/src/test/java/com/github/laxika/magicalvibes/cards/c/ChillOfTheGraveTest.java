package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.cards.s.SelhoffEntomber;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChillOfTheGrave.class, DawnhartDisciple.class, SelhoffEntomber.class, Island.class, Abrade.class})
class ChillOfTheGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a creature, skips its next untap, and draws a card")
    void tapsLocksAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());
        harness.setHand(player1, List.of(new ChillOfTheGrave()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Chill of the Grave");
    }

    @Test
    @DisplayName("Costs {1}{U} when controlling a Zombie")
    void costsOneLessWithZombie() {
        harness.addToBattlefield(player1, new SelhoffEntomber());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());
        harness.setHand(player1, List.of(new ChillOfTheGrave()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ChillOfTheGrave()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Skips only the target controller's next untap step")
    void skipsOnlyNextControllerUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());
        harness.setHand(player1, List.of(new ChillOfTheGrave()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target an already tapped creature you control and still draw")
    void alreadyTappedOwnCreatureStillLocksAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        target.setTapped(true);
        harness.setHand(player1, List.of(new ChillOfTheGrave()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot cast for two mana without a Zombie")
    void requiresFullCostWithoutZombie() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());
        harness.setHand(player1, List.of(new ChillOfTheGrave()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Zombie does not reduce the cost")
    void opponentsZombieDoesNotReduceCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SelhoffEntomber());
        harness.setHand(player1, List.of(new ChillOfTheGrave()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Zombies reduce the cost by only one mana")
    void multipleZombiesDoNotIncreaseReduction() {
        harness.addToBattlefield(player1, new SelhoffEntomber());
        harness.addToBattlefield(player1, new SelhoffEntomber());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());
        harness.setHand(player1, List.of(new ChillOfTheGrave()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("The Zombie discount does not remove the blue mana requirement")
    void reductionDoesNotRemoveBlueRequirement() {
        harness.addToBattlefield(player1, new SelhoffEntomber());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());
        harness.setHand(player1, List.of(new ChillOfTheGrave()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not draw when the target is removed in response")
    void removedTargetPreventsDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());
        harness.setHand(player1, List.of(new ChillOfTheGrave()));
        harness.setHand(player2, List.of(new Abrade()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Dawnhart Disciple");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Chill of the Grave");
    }
}
