package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.t.TrustedForcemage;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({VanguardsShield.class, TrustedForcemage.class})
class VanguardsShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +0/+3")
    void equippedCreatureGetsToughnessBoost() {
        Permanent creature = addCreatureReady(player1, new TrustedForcemage());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Unequipped creatures are unaffected")
    void otherCreatureUnaffected() {
        Permanent creature = addCreatureReady(player1, new TrustedForcemage());
        Permanent other = addCreatureReady(player1, new TrustedForcemage());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip ability attaches the shield to target creature")
    void equipAttaches() {
        Permanent shield = addShieldReady(player1);
        Permanent creature = addCreatureReady(player1, new TrustedForcemage());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equipped creature can block two attackers")
    void equippedCreatureBlocksTwo() {
        Permanent blocker = addCreatureReady(player2, new TrustedForcemage());
        Permanent shield = addShieldReady(player2);
        shield.setAttachedTo(blocker.getId());
        addAttackers(2);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ));

        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Unequipped creature cannot block two attackers")
    void unequippedCreatureCannotBlockTwo() {
        Permanent blocker = addCreatureReady(player2, new TrustedForcemage());
        addShieldReady(player2);
        addAttackers(2);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Equipped creature cannot block three attackers")
    void equippedCreatureCannotBlockThree() {
        Permanent blocker = addCreatureReady(player2, new TrustedForcemage());
        Permanent shield = addShieldReady(player2);
        shield.setAttachedTo(blocker.getId());
        addAttackers(3);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Boost is lost when the shield leaves the battlefield")
    void boostLostWhenShieldRemoved() {
        Permanent creature = addCreatureReady(player1, new TrustedForcemage());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(shield);

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Shield still grants an additional block after its controller changes")
    void additionalBlockSurvivesShieldControlChange() {
        Permanent blocker = addCreatureReady(player2, new TrustedForcemage());
        Permanent shield = addShieldReady(player2);
        shield.setAttachedTo(blocker.getId());
        addAttackers(2);

        gd.playerBattlefields.get(player2.getId()).remove(shield);
        gd.playerBattlefields.get(player1.getId()).add(shield);
        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ));

        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Two shields grant cumulative toughness and additional blocks")
    void twoShieldsAllowThreeBlocks() {
        Permanent blocker = addCreatureReady(player2, new TrustedForcemage());
        addShieldReady(player2).setAttachedTo(blocker.getId());
        addShieldReady(player2).setAttachedTo(blocker.getId());
        addAttackers(3);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(8);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2)
        ));

        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @DisplayName("Reequipping moves the toughness bonus to the new creature")
    void reequippingMovesBonus() {
        Permanent shield = addShieldReady(player1);
        Permanent original = addCreatureReady(player1, new TrustedForcemage());
        Permanent replacement = addCreatureReady(player1, new TrustedForcemage());
        shield.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, replacement.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(5);
    }

    private Permanent addShieldReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new VanguardsShield());
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent atk = addCreatureReady(player1, new TrustedForcemage());
            atk.setAttacking(true);
        }
    }

}
