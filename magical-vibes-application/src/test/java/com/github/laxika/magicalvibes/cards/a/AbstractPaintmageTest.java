package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FlowState;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbstractPaintmage.class, Shock.class, FlowState.class})
class AbstractPaintmageTest extends BaseCardTest {

    @Test
    @DisplayName("Precombat main trigger adds one instant/sorcery-only blue and red mana")
    void precombatMainTriggerAddsRestrictedMana() {
        addReadyPaintmage(player1);

        advanceToPrecombatMain(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(0);
        assertThat(pool.get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Restricted mana can pay for an instant spell")
    void restrictedManaPaysForInstant() {
        addReadyPaintmage(player1);
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, findPermanent(player1, "Abstract Paintmage").getId());
        harness.passBothPriorities();

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(0);
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana cannot pay for a creature spell")
    void restrictedManaCannotPayForCreature() {
        addReadyPaintmage(player1);
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new AbstractPaintmage()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger on opponent's precombat main phase")
    void doesNotTriggerOnOpponentsTurn() {
        addReadyPaintmage(player1);

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers again on a later turn")
    void triggersOnLaterTurn() {
        harness.setLibrary(player1, List.of(new AbstractPaintmage()));
        harness.setLibrary(player2, List.of(new AbstractPaintmage()));
        addReadyPaintmage(player1);
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Both colors of restricted mana can pay for a sorcery's colored and generic costs")
    void restrictedManaPaysForSorcery() {
        addReadyPaintmage(player1);
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new FlowState()));
        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isZero();
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The trigger still adds mana after its source leaves the battlefield")
    void triggerResolvesAfterSourceDies() {
        addReadyPaintmage(player1);
        advanceToPrecombatMain(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, findPermanent(player1, "Abstract Paintmage").getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Abstract Paintmage");
        harness.passBothPriorities();

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getInstantSorceryOnlyColoredTotal()).isZero();
    }

    @Test
    @DisplayName("Unspent mana empties at the end of the main phase and no mana is added in the second main phase")
    void manaEmptiesAndDoesNotTriggerInSecondMainPhase() {
        addReadyPaintmage(player1);
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColoredTotal()).isZero();
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColoredTotal()).isZero();
    }

    private void addReadyPaintmage(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new AbstractPaintmage());
    }

    private void advanceToPrecombatMain(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
