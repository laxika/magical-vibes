package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.ThunderSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Chromium.class, ThunderSpirit.class})
class ChromiumTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rampage 2 grants no bonus")
    void oneBlockerGivesNothing() {
        Permanent chromium = addCreatureReady(player1, new Chromium());
        addCreatureReady(player2, new ThunderSpirit());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(chromium.getPowerModifier()).isZero();
        assertThat(chromium.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With two blockers Rampage 2 grants +2/+2 until end of turn")
    void twoBlockersGivePlusTwo() {
        Permanent chromium = addCreatureReady(player1, new Chromium());
        addCreatureReady(player2, new ThunderSpirit());
        addCreatureReady(player2, new ThunderSpirit());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(chromium.getPowerModifier()).isEqualTo(2);
        assertThat(chromium.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("With three blockers Rampage 2 grants +4/+4 until end of turn")
    void threeBlockersGivePlusFour() {
        Permanent chromium = addCreatureReady(player1, new Chromium());
        addCreatureReady(player2, new ThunderSpirit());
        addCreatureReady(player2, new ThunderSpirit());
        addCreatureReady(player2, new ThunderSpirit());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(chromium.getPowerModifier()).isEqualTo(4);
        assertThat(chromium.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Rampage 2's bonus wears off at end of turn")
    void rampageBonusWearsOffAtEndOfTurn() {
        Permanent chromium = addCreatureReady(player1, new Chromium());
        addCreatureReady(player2, new ThunderSpirit());
        addCreatureReady(player2, new ThunderSpirit());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(chromium.getPowerModifier()).isEqualTo(2);
        assertThat(chromium.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(chromium.getPowerModifier()).isZero();
        assertThat(chromium.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Chromium")
    void decliningUpkeepPaymentSacrificesChromium() {
        harness.addToBattlefield(player1, new Chromium());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Chromium");
    }

    @Test
    @DisplayName("Paying {W}{U}{B} during upkeep keeps Chromium")
    void payingUpkeepPaymentKeepsChromium() {
        harness.addToBattlefield(player1, new Chromium());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Chromium");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Choosing to pay without enough mana still sacrifices Chromium")
    void insufficientUpkeepPaymentSacrificesChromium() {
        harness.addToBattlefield(player1, new Chromium());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Chromium");
    }
}
