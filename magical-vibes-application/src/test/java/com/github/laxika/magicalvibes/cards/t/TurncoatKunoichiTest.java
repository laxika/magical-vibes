package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();

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
}
