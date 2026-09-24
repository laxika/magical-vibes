package com.github.laxika.magicalvibes.cards.w;

import java.util.List;

import com.github.laxika.magicalvibes.cards.i.ImperiousPerfect;
import com.github.laxika.magicalvibes.cards.k.KrosanDrover;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WirewoodSymbiote.class, KrosanDrover.class, ScornfulEgotist.class, TempleOfTheFalseGod.class,
        ImperiousPerfect.class})
class WirewoodSymbioteTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an Elf and untaps the target creature")
    void returnsElfAndUntapsTarget() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        harness.addToBattlefield(player1, new KrosanDrover());
        Permanent targetCreature = addCreatureReady(player1, new ScornfulEgotist());
        targetCreature.tap();

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Krosan Drover");
        harness.assertNotOnBattlefield(player1, "Krosan Drover");
        assertThat(targetCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With multiple Elves, chooses which one to return")
    void choosesWhichElfToReturn() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        Permanent elf1 = harness.addToBattlefieldAndReturn(player1, new KrosanDrover());
        Permanent elf2 = harness.addToBattlefieldAndReturn(player1, new KrosanDrover());
        Permanent targetCreature = addCreatureReady(player1, new ScornfulEgotist());
        targetCreature.tap();

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.handlePermanentChosen(player1, elf2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Krosan Drover").getId()).isEqualTo(elf1.getId());
        harness.assertInHand(player1, "Krosan Drover");
        assertThat(targetCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without an Elf you control")
    void cannotActivateWithoutElf() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        Permanent targetCreature = addCreatureReady(player1, new ScornfulEgotist());
        targetCreature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(targetCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can be activated only once each turn")
    void onlyOncePerTurn() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        harness.addToBattlefield(player1, new KrosanDrover());
        harness.addToBattlefield(player1, new KrosanDrover());
        Permanent targetCreature = addCreatureReady(player1, new ScornfulEgotist());
        targetCreature.tap();

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.handlePermanentChosen(player1, findPermanent(player1, "Krosan Drover").getId());
        harness.passBothPriorities();

        targetCreature.tap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be activated again on a later turn")
    void canActivateAgainOnLaterTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        Permanent elf1 = harness.addToBattlefieldAndReturn(player1, new KrosanDrover());
        harness.addToBattlefield(player1, new KrosanDrover());
        Permanent targetCreature = addCreatureReady(player1, new ScornfulEgotist());
        targetCreature.tap();

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.handlePermanentChosen(player1, elf1.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        targetCreature.tap();
        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(targetCreature.isTapped()).isFalse();
        harness.assertInHand(player1, "Krosan Drover");
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        harness.addToBattlefield(player1, new KrosanDrover());
        Permanent targetCreature = addCreatureReady(player2, new ScornfulEgotist());
        targetCreature.tap();

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(targetCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot pay the cost with an Elf an opponent controls")
    void cannotUseOpponentsElfForCost() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        harness.addToBattlefield(player2, new KrosanDrover());
        Permanent targetCreature = addCreatureReady(player1, new ScornfulEgotist());
        targetCreature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Krosan Drover");
        assertThat(targetCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        harness.addToBattlefield(player1, new KrosanDrover());
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new TempleOfTheFalseGod());
        temple.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, temple.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(temple.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Krosan Drover");
        harness.assertOnBattlefield(player1, "Temple of the False God");
    }

    @Test
    @DisplayName("Returning an Elf token does not put it into its owner's hand")
    void returningElfTokenDoesNotPutItInHand() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        addCreatureReady(player1, new ImperiousPerfect());
        Permanent targetCreature = addCreatureReady(player1, new ScornfulEgotist());
        targetCreature.tap();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Elf Warrior");

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elf Warrior");
        harness.assertNotInHand(player1, "Elf Warrior");
    }
}
