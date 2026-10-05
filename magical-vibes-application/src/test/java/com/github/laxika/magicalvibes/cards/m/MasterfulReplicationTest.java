package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MasterfulReplication.class, Forest.class, GrizzlyBears.class, ZuranOrb.class,
        MyrTurbine.class, ManifoldKey.class, MeteorGolem.class, Unsummon.class})
class MasterfulReplicationTest extends BaseCardTest {

    @Test
    @DisplayName("The token mode creates two Golem tokens")
    void tokenModeCreatesTwoGolems() {
        castMasterfulReplication(0, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("The copy mode gives each other artifact you control the target artifact's ability")
    void copyModeAffectsOtherControlledArtifacts() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ZuranOrb());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new Forest());

        castMasterfulReplication(1, List.of(target.getId()));
        prepareMainPhase(player1);
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(otherArtifact.getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("The temporary copies end at cleanup")
    void copyModeEndsAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ZuranOrb());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new MyrTurbine());

        castMasterfulReplication(1, List.of(target.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new Forest());
        prepareMainPhase(player1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
        assertThat(otherArtifact.getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("The copy mode only accepts an artifact you control as its target")
    void copyModeRejectsNonArtifactTarget() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        giveMasterfulReplication();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact you control");
    }

    @Test
    @DisplayName("The created tokens are untapped colorless 3/3 Golem artifact creatures")
    void tokenModeCreatesCorrectTokenCharacteristics() {
        castMasterfulReplication(0, List.of());

        assertThat(findPermanents(player1, "Golem")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOLEM);
            assertThat(token.getCard().getColors()).isEmpty();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Copying affects only existing controlled artifacts and does not trigger entering abilities")
    void copyModeLeavesOtherPermanentsAndLaterArtifactsUnchanged() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MeteorGolem());
        Permanent key = harness.addToBattlefieldAndReturn(player1, new ManifoldKey());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentKey = harness.addToBattlefieldAndReturn(player2, new ManifoldKey());
        key.setTapped(true);

        castMasterfulReplication(1, List.of(target.getId()));

        assertThat(key.getCard().getName()).isEqualTo("Meteor Golem");
        assertThat(key.isTapped()).isTrue();
        assertThat(forest.getCard().getName()).isEqualTo("Forest");
        assertThat(opponentKey.getCard().getName()).isEqualTo("Manifold Key");
        harness.assertOnBattlefield(player2, "Manifold Key");
        assertThat(gd.stack).isEmpty();

        harness.addToBattlefield(player1, new ManifoldKey());
        assertThat(countPermanents(player1, "Manifold Key")).isEqualTo(1);
    }

    @Test
    @DisplayName("Copying an artifact creature onto Golem tokens preserves their token status")
    void copyModePreservesTokenStatus() {
        castMasterfulReplication(0, List.of());
        List<Permanent> tokens = findPermanents(player1, "Golem");
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MeteorGolem());

        castMasterfulReplication(1, List.of(target.getId()));

        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getName()).isEqualTo("Meteor Golem");
            assertThat(token.getCard().isToken()).isTrue();
        });
        assertThat(target.getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("Copies retain their own counters without copying the target's counters")
    void copyModeDoesNotCopyCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MeteorGolem());
        Permanent key = harness.addToBattlefieldAndReturn(player1, new ManifoldKey());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        key.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castMasterfulReplication(1, List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, key)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, key)).isEqualTo(4);
        assertThat(key.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("An artifact already copying another artifact supplies its copied characteristics")
    void copyModeCopiesAnExistingCopy() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new MeteorGolem());
        Permanent firstKey = harness.addToBattlefieldAndReturn(player1, new ManifoldKey());
        castMasterfulReplication(1, List.of(original.getId()));
        Permanent laterKey = harness.addToBattlefieldAndReturn(player1, new ManifoldKey());

        castMasterfulReplication(1, List.of(firstKey.getId()));

        assertThat(laterKey.getCard().getName()).isEqualTo("Meteor Golem");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(firstKey.getCard().getName()).isEqualTo("Manifold Key");
        assertThat(laterKey.getCard().getName()).isEqualTo("Manifold Key");
        assertThat(original.getCard().getName()).isEqualTo("Meteor Golem");
    }

    @Test
    @DisplayName("An opponent's artifact cannot be chosen for the copy mode")
    void copyModeRejectsOpponentArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManifoldKey());
        giveMasterfulReplication();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The copy mode does not resolve when its target leaves the battlefield")
    void copyModeDoesNotResolveAfterTargetIsReturned() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MeteorGolem());
        Permanent key = harness.addToBattlefieldAndReturn(player1, new ManifoldKey());
        giveMasterfulReplication();
        harness.castInstant(player1, 0, 1, target.getId());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Meteor Golem");
        assertThat(key.getCard().getName()).isEqualTo("Manifold Key");
        harness.assertInGraveyard(player1, "Masterful Replication");
    }

    private void castMasterfulReplication(int modeIndex, List<UUID> targetIds) {
        giveMasterfulReplication();
        harness.castInstant(player1, 0, modeIndex, targetIds.isEmpty() ? null : targetIds.getFirst());
        harness.passBothPriorities();
    }

    private void giveMasterfulReplication() {
        harness.setHand(player1, List.of(new MasterfulReplication()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
