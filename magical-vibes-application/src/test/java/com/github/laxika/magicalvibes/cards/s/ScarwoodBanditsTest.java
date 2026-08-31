package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.f.FellwarStone;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScarwoodBandits.class, FellwarStone.class, BogRats.class, Forest.class})
class ScarwoodBanditsTest extends BaseCardTest {

    @Test
    void declinesPaymentAndGainsControlOfArtifact() {
        Permanent bandits = addBandits();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FellwarStone());

        activate(bandits, artifact);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    void paymentPreventsControlChange() {
        Permanent bandits = addBandits();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FellwarStone());

        activate(bandits, artifact);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void controlReturnsWhenBanditsLeavesTheBattlefield() {
        Permanent bandits = addBandits();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FellwarStone());

        activate(bandits, artifact);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bandits));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    void cannotTargetNonartifactPermanent() {
        Permanent bandits = addBandits();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BogRats());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(bandits),
                null,
                creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anOpponentMustBeOfferedPaymentForAnArtifactYouControl() {
        Permanent bandits = addBandits();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FellwarStone());

        activate(bandits, artifact);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
    }

    @Test
    void forestwalkPreventsBlockingWhenDefenderControlsForest() {
        harness.addToBattlefield(player2, new Forest());
        Permanent bandits = addCreatureReady(player1, new ScarwoodBandits());
        bandits.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BogRats());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bandits);
        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void forestwalkAllowsBlockingWhenDefenderControlsNoForest() {
        Permanent bandits = addCreatureReady(player1, new ScarwoodBandits());
        bandits.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BogRats());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bandits);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addBandits() {
        return addCreatureReady(player1, new ScarwoodBandits());
    }

    private void activate(Permanent bandits, Permanent artifact) {
        addActivationMana();
        harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(bandits),
                null,
                artifact.getId());
        harness.passBothPriorities();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
