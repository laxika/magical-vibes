package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TurncoatKunoichi.class, GrizzlyBears.class})
class TurncoatKunoichiTest extends BaseCardTest {

    @Test
    @DisplayName("Ordinary cast exiles an opposing creature until Turncoat Kunoichi leaves")
    void ordinaryCastReturnsTheCreatureWhenKunoichiLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TurncoatKunoichi()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        Permanent kunoichi = findPermanent(player1, "Turncoat Kunoichi");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, kunoichi));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Sneak cast exiles the opposing creature permanently")
    void sneakCastDoesNotReturnTheCreatureWhenKunoichiLeaves() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TurncoatKunoichi()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(attacker.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        Permanent kunoichi = findPermanent(player1, "Turncoat Kunoichi");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, kunoichi));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurncoatKunoichi()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Leaving before the enter trigger resolves only prevents ordinary exile")
    void sourceLeavesBeforeEnterTriggerResolves(boolean sneak) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurncoatKunoichi()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        if (sneak) {
            Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            attacker.setAttackTarget(player2.getId());
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(attacker.getId()));
            harness.assertInHand(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        } else {
            harness.castCreature(player1, 0, 0, target.getId());
        }

        harness.passBothPriorities();
        Permanent kunoichi = findPermanent(player1, "Turncoat Kunoichi");
        if (sneak) {
            assertThat(kunoichi.isTapped()).isTrue();
            assertThat(kunoichi.isAttacking()).isTrue();
            assertThat(kunoichi.getAttackTarget()).isEqualTo(player2.getId());
        }
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, kunoichi));
        resolveAllTriggers();

        if (sneak) {
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            assertThat(gd.getPlayerExiledCards(player2.getId()))
                    .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        } else {
            harness.assertOnBattlefield(player2, "Grizzly Bears");
            assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        }
    }

    @Test
    @DisplayName("Can be cast when no opposing creature can be targeted")
    void canBeCastWithoutOpposingCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurncoatKunoichi()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Turncoat Kunoichi");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
