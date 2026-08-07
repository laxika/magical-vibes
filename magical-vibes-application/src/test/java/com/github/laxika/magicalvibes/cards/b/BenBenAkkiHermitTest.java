package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BenBenAkkiHermitTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of untapped Mountains, killing the attacker")
    void dealsDamageEqualToUntappedMountains() {
        addBenBen(player1);
        addMountains(player1, 2, false);
        Permanent attacker = addAttackingCreature(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tapped Mountains are not counted")
    void tappedMountainsAreNotCounted() {
        addBenBen(player1);
        addMountains(player1, 1, false);
        addMountains(player1, 3, true);
        Permanent attacker = addAttackingCreature(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("deals 1 damage"));
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addBenBen(player1);
        addMountains(player1, 2, false);
        GrizzlyBears bear = new GrizzlyBears();
        Permanent perm = new Permanent(bear);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(perm);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, perm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking");
    }

    private void addBenBen(Player player) {
        Permanent perm = new Permanent(new BenBenAkkiHermit());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
    }

    private void addMountains(Player player, int count, boolean tapped) {
        for (int i = 0; i < count; i++) {
            Permanent perm = new Permanent(new Mountain());
            if (tapped) {
                perm.tap();
            }
            harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        }
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
