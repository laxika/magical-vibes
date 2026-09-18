package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BattlewiseAven;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnuridSwarmsnapper.class, BattlewiseAven.class})
class AnuridSwarmsnapperTest extends BaseCardTest {

    @Test
    @DisplayName("Anurid Swarmsnapper cannot block two creatures without activating its ability")
    void cannotBlockTwoCreaturesWithoutActivation() {
        Permanent swarmsnapper = addSwarmsnapper();
        addAttackers(2);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(swarmsnapper);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating Anurid Swarmsnapper lets it block two creatures")
    void blocksTwoCreaturesAfterActivating() {
        Permanent swarmsnapper = addSwarmsnapper();
        addAttackers(2);

        activate(swarmsnapper);
        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(swarmsnapper);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ));

        assertThat(swarmsnapper.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Activating Anurid Swarmsnapper twice lets it block three creatures")
    void blocksThreeCreaturesAfterActivatingTwice() {
        Permanent swarmsnapper = addSwarmsnapper();
        addAttackers(3);

        activate(swarmsnapper);
        activate(swarmsnapper);
        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(swarmsnapper);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2)
        ));

        assertThat(swarmsnapper.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @DisplayName("Reach lets Anurid Swarmsnapper block a creature with flying")
    void blocksFlyingCreatureWithReach() {
        Permanent swarmsnapper = addSwarmsnapper();
        addAttacker(new BattlewiseAven());

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(swarmsnapper);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, 0)));

        assertThat(swarmsnapper.getBlockingTargets()).containsExactly(0);
    }

    @Test
    @DisplayName("The additional block grant expires at end of turn")
    void grantExpiresAtEndOfTurn() {
        Permanent swarmsnapper = addSwarmsnapper();
        activate(swarmsnapper);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        addAttackers(2);
        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(swarmsnapper);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ))).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSwarmsnapper() {
        return addCreatureReady(player2, new AnuridSwarmsnapper());
    }

    private void activate(Permanent swarmsnapper) {
        int idx = gd.playerBattlefields.get(player2.getId()).indexOf(swarmsnapper);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, idx, null, null);
        harness.passBothPriorities();
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            addAttacker(new BattlewiseAven());
        }
    }

    private Permanent addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }
}
