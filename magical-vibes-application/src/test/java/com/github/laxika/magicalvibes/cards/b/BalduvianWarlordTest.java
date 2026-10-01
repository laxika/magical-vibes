package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.k.KjeldoranOutrider;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalduvianWarlord.class, KjeldoranOutrider.class})
class BalduvianWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("removes a blocker, unblocks its former attacker, and reassigns it")
    void removesAndReassignsBlocker() {
        Permanent warlord = addCreatureReady(player2, new BalduvianWarlord());
        Permanent formerAttacker = addCreatureReady(player1, new KjeldoranOutrider());
        Permanent chosenAttacker = addCreatureReady(player1, new KjeldoranOutrider());
        Permanent blocker = addCreatureReady(player2, new KjeldoranOutrider());

        setUpCombat(formerAttacker, chosenAttacker, blocker);

        harness.activateAbility(player2, indexOf(player2, warlord), null,
                blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(formerAttacker.isBlockedWithoutBlockers()).isFalse();

        harness.handlePermanentChosen(player2, chosenAttacker.getId());

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).containsExactly(chosenAttacker.getId());
        assertThat(formerAttacker.isBlockedWithoutBlockers()).isFalse();
        assertThat(warlord.isTapped()).isTrue();
    }

    @Test
    @DisplayName("keeps a former attacker blocked when another blocker blocked it")
    void keepsFormerAttackerBlockedWithAnotherBlocker() {
        Permanent warlord = addCreatureReady(player2, new BalduvianWarlord());
        Permanent formerAttacker = addCreatureReady(player1, new KjeldoranOutrider());
        Permanent chosenAttacker = addCreatureReady(player1, new KjeldoranOutrider());
        Permanent blocker = addCreatureReady(player2, new KjeldoranOutrider());
        Permanent otherBlocker = addCreatureReady(player2, new KjeldoranOutrider());

        setUpCombat(formerAttacker, chosenAttacker, List.of(blocker, otherBlocker));

        harness.activateAbility(player2, indexOf(player2, warlord), null,
                blocker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, chosenAttacker.getId());

        assertThat(otherBlocker.getBlockingTargetIds()).containsExactly(formerAttacker.getId());
        assertThat(formerAttacker.isBlockedWithoutBlockers()).isFalse();
        assertThat(blocker.getBlockingTargetIds()).containsExactly(chosenAttacker.getId());
    }

    @Test
    @DisplayName("does not reassign the blocker when no legal attacker remains")
    void doesNotReassignWhenNoAttackerCanBeBlocked() {
        Permanent warlord = addCreatureReady(player2, new BalduvianWarlord());
        Permanent formerAttacker = addCreatureReady(player1, new KjeldoranOutrider());
        Permanent blocker = addCreatureReady(player2, new KjeldoranOutrider());

        setUpCombat(formerAttacker, blocker);
        formerAttacker.getGrantedKeywords().add(Keyword.FLYING);

        harness.activateAbility(player2, indexOf(player2, warlord), null,
                blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(formerAttacker.isBlockedWithoutBlockers()).isFalse();
    }

    @Test
    @DisplayName("keeps an attacker blocked after its other blocker left combat earlier")
    void keepsAttackerBlockedAfterOtherBlockerLeavesCombat() {
        Permanent firstWarlord = addCreatureReady(player2, new BalduvianWarlord());
        Permanent secondWarlord = addCreatureReady(player2, new BalduvianWarlord());
        Permanent attacker = addCreatureReady(player1, new KjeldoranOutrider());
        Permanent blocker = addCreatureReady(player2, new KjeldoranOutrider());
        Permanent otherBlocker = addCreatureReady(player2, new KjeldoranOutrider());

        setUpCombat(attacker, List.of(blocker, otherBlocker));
        attacker.getGrantedKeywords().add(Keyword.FLYING);

        harness.activateAbility(player2, indexOf(player2, firstWarlord), null, otherBlocker.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);
        assertThat(otherBlocker.isBlocking()).isFalse();
        assertThat(attacker.isBlockedWithoutBlockers()).isFalse();

        harness.activateAbility(player2, indexOf(player2, secondWarlord), null, blocker.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();
    }

    @Test
    @DisplayName("can remove and reassign a blocker to the same attacker")
    void canReblockTheFormerAttacker() {
        Permanent warlord = addCreatureReady(player2, new BalduvianWarlord());
        Permanent attacker = addCreatureReady(player1, new KjeldoranOutrider());
        Permanent blocker = addCreatureReady(player2, new KjeldoranOutrider());

        setUpCombat(attacker, blocker);

        harness.activateAbility(player2, indexOf(player2, warlord), null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(attacker.isBlockedWithoutBlockers()).isFalse();
    }

    @Test
    @DisplayName("rejects a target that is not a blocking creature")
    void rejectsNonblockingTarget() {
        Permanent warlord = addCreatureReady(player2, new BalduvianWarlord());
        Permanent attacker = addCreatureReady(player1, new KjeldoranOutrider());
        Permanent bystander = addCreatureReady(player2, new KjeldoranOutrider());

        declareAttackers(List.of(indexOf(player1, attacker)));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(
                player2, indexOf(player2, warlord), null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking creature");
    }

    @Test
    @DisplayName("can only be activated during the declare blockers step")
    void rejectsActivationOutsideDeclareBlockers() {
        Permanent warlord = addCreatureReady(player2, new BalduvianWarlord());
        Permanent attacker = addCreatureReady(player1, new KjeldoranOutrider());
        Permanent blocker = addCreatureReady(player2, new KjeldoranOutrider());

        setUpCombat(attacker, blocker);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        assertThatThrownBy(() -> harness.activateAbility(
                player2, indexOf(player2, warlord), null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("declare blockers step");
    }

    private void setUpCombat(Permanent formerAttacker, Permanent chosenAttacker, Permanent blocker) {
        setUpCombat(formerAttacker, chosenAttacker, List.of(blocker));
    }

    private void setUpCombat(Permanent formerAttacker, Permanent chosenAttacker, List<Permanent> blockers) {
        declareAttackersAndPrepareBlockers(List.of(
                indexOf(player1, formerAttacker),
                indexOf(player1, chosenAttacker)));
        gs.declareBlockers(gd, player2, blockers.stream()
                .map(blocker -> new BlockerAssignment(indexOf(player2, blocker), indexOf(player1, formerAttacker)))
                .toList());
        harness.clearPriorityPassed();
    }

    private void setUpCombat(Permanent formerAttacker, List<Permanent> blockers) {
        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, formerAttacker)));
        gs.declareBlockers(gd, player2, blockers.stream()
                .map(blocker -> new BlockerAssignment(indexOf(player2, blocker), indexOf(player1, formerAttacker)))
                .toList());
        harness.clearPriorityPassed();
    }

    private void setUpCombat(Permanent formerAttacker, Permanent blocker) {
        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, formerAttacker)));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(indexOf(player2, blocker), indexOf(player1, formerAttacker))));
        harness.clearPriorityPassed();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
