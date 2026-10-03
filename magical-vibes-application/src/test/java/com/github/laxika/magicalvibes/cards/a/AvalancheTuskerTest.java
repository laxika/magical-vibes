package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BribersPurse;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvalancheTusker.class, AlpineGrizzly.class, BribersPurse.class})
class AvalancheTuskerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking targets a creature defending player controls and forces it to block")
    void attacksAndForcesDefendingCreatureToBlock() {
        Permanent tusker = addCreatureReady(player1, new AvalancheTusker());
        Permanent defendingCreature = addCreatureReady(player2, new AlpineGrizzly());
        Permanent ownCreature = addCreatureReady(player1, new AlpineGrizzly());
        Permanent defendingNoncreature = harness.addToBattlefieldAndReturn(player2, new BribersPurse());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(defendingCreature.getId())
                .doesNotContain(tusker.getId(), ownCreature.getId(), defendingNoncreature.getId());

        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();

        assertThat(defendingCreature.getMustBlockIds()).containsExactly(tusker.getId());
    }

    @Test
    @DisplayName("The targeted creature must be declared as a blocker")
    void targetedCreatureMustBlock() {
        addCreatureReady(player1, new AvalancheTusker());
        Permanent defendingCreature = addCreatureReady(player2, new AlpineGrizzly());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(defendingCreature.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tapped creature is a legal target but is not forced to block")
    void tappedTargetCannotBlock() {
        addCreatureReady(player1, new AvalancheTusker());
        Permanent defender = addCreatureReady(player2, new AlpineGrizzly());
        defender.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        assertThat(defender.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A creature with summoning sickness must still block if able")
    void summoningSickTargetMustBlock() {
        addCreatureReady(player1, new AvalancheTusker());
        Permanent defender = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        harness.passBothPriorities();
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(defender.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A previous combat's target need not block in an additional combat")
    void blockingRequirementExpiresAtEndOfCombat() {
        Permanent tusker = addCreatureReady(player1, new AvalancheTusker());
        Permanent firstTarget = addCreatureReady(player2, new AlpineGrizzly());
        Permanent secondTarget = addCreatureReady(player2, new AlpineGrizzly());
        firstTarget.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.passBothPriorities();
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            gs.declareBlockers(gd, player2, List.of());
            harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        });

        tusker.untap();
        firstTarget.untap();
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        assertThat(firstTarget.isBlocking()).isFalse();
    }
}
