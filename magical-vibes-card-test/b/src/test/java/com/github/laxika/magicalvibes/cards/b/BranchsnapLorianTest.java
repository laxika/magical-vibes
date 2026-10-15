package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BranchsnapLorian.class, AvenEnvoy.class})
class BranchsnapLorianTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        BranchsnapLorian card = new BranchsnapLorian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent lorian = findPermanent(player1, "Branchsnap Lorian");
        assertThat(lorian.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lorian));
        harness.passBothPriorities();

        assertThat(lorian.isFaceDown()).isFalse();
    }

    @Test
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new BranchsnapLorian());
        Permanent blocker = addCreatureReady(player2, new AvenEnvoy());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2
        ));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    void colorlessManaCannotPayTheMorphCost() {
        Permanent lorian = castFaceDown();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(lorian)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(lorian.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void faceDownCreatureDoesNotTrample() {
        Permanent attacker = castFaceDown();
        attacker.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new BranchsnapLorian());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    void turningFaceUpBeforeCombatDamageImmediatelyEnablesTrample() {
        Permanent attacker = castFaceDown();
        attacker.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new AvenEnvoy());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(attacker));

        assertThat(attacker.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();

        resolveCombat();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2
        ));

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new BranchsnapLorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castCreatureWithMorph(player1, 0);
            harness.passBothPriorities();
        });
        return findPermanent(player1, "Branchsnap Lorian");
    }
}
