package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirriWeatherlightDuelist.class, GrizzlyBears.class, SwordsToPlowshares.class})
class MirriWeatherlightDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped Mirri limits attacks against her controller to one creature")
    void tappedMirriLimitsAttacksAgainstController() {
        Permanent mirri = addCreatureReady(player2, new MirriWeatherlightDuelist());
        mirri.tap();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 creature can attack");
    }

    @Test
    @DisplayName("An untapped Mirri does not limit attacks against her controller")
    void untappedMirriDoesNotLimitAttacksAgainstController() {
        addCreatureReady(player2, new MirriWeatherlightDuelist());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("When Mirri attacks, each opponent can block with only one creature this combat")
    void attackTriggerLimitsBlockers() {
        addCreatureReady(player1, new MirriWeatherlightDuelist());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 distinct creature can block each combat");
    }

    @Test
    void tappedMirriAllowsOneAttacker() {
        addCreatureReady(player2, new MirriWeatherlightDuelist()).tap();
        addCreatureReady(player1, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(List.of(0))).doesNotThrowAnyException();
    }

    @Test
    void untappingMirriRemovesAttackRestriction() {
        Permanent mirri = addCreatureReady(player2, new MirriWeatherlightDuelist());
        mirri.tap();
        mirri.untap();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(List.of(0, 1))).doesNotThrowAnyException();
    }

    @Test
    void tappedMirriDoesNotLimitHerControllersAttackers() {
        addCreatureReady(player1, new MirriWeatherlightDuelist()).tap();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(List.of(1, 2))).doesNotThrowAnyException();
    }

    @Test
    void tappingMirriAfterDeclarationDoesNotRemoveAttackers() {
        Permanent mirri = addCreatureReady(player2, new MirriWeatherlightDuelist());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        mirri.tap();

        assertThat(first.isAttacking()).isTrue();
        assertThat(second.isAttacking()).isTrue();
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void mirriNotAttackingDoesNotLimitBlockers() {
        addCreatureReady(player1, new MirriWeatherlightDuelist());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1), new BlockerAssignment(1, 1))))
                .doesNotThrowAnyException();
    }

    @Test
    void attackRestrictionAppliesAcrossDifferentAttackers() {
        addCreatureReady(player1, new MirriWeatherlightDuelist());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1)));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 distinct creature can block each combat");
    }

    @Test
    void firstStrikeKillsBlockerBeforeItCanDamageMirri() {
        addCreatureReady(player1, new MirriWeatherlightDuelist());
        addCreatureReady(player2, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Mirri, Weatherlight Duelist");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void blockerRestrictionSurvivesRemovalBeforeTriggerResolves() {
        assertRestrictionSurvivesRemoval(false);
    }

    @Test
    void blockerRestrictionSurvivesRemovalAfterTriggerResolves() {
        assertRestrictionSurvivesRemoval(true);
    }

    private void assertRestrictionSurvivesRemoval(boolean resolveTriggerFirst) {
        Permanent mirri = addCreatureReady(player1, new MirriWeatherlightDuelist());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1, 2)));
        if (resolveTriggerFirst) {
            harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        }
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.castAndResolveInstant(player2, 0, mirri.getId()));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        harness.assertNotOnBattlefield(player1, "Mirri, Weatherlight Duelist");
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 distinct creature can block each combat");
    }
}
