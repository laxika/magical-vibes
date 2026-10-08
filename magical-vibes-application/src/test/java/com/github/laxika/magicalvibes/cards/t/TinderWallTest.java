package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TinderWall.class, BalduvianBears.class})
class TinderWallTest extends BaseCardTest {

    @Test
    @DisplayName("Defender prevents Tinder Wall from attacking")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new TinderWall());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Sacrificing Tinder Wall adds two red mana")
    void sacrificeAddsTwoRedMana() {
        addCreatureReady(player1, new TinderWall());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Tinder Wall");
    }

    @Test
    @DisplayName("{R}, Sacrifice: deals 2 damage to the creature Tinder Wall is blocking")
    void damagesBlockedCreature() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player2, new TinderWall());

        blockWithWall();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Tinder Wall");
    }

    @Test
    @DisplayName("Damage ability cannot target a creature Tinder Wall isn't blocking")
    void cannotTargetUnblockedCreature() {
        addCreatureReady(player1, new BalduvianBears());
        Permanent otherAttacker = addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player2, new TinderWall());
        otherAttacker.setAttacking(true);

        blockWithWall();
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, otherAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Tinder Wall can produce mana immediately")
    void tappedSummoningSickWallProducesManaWithoutUsingStack() {
        Permanent wall = addCreatureReady(player1, new TinderWall());
        wall.setSummoningSick(true);
        wall.tap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Tinder Wall");
        harness.assertNotOnBattlefield(player1, "Tinder Wall");
    }

    @Test
    @DisplayName("Damage ability requires red mana before sacrificing Tinder Wall")
    void cannotPayDamageCostWithoutRedMana() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player2, new TinderWall());
        blockWithWall();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Tinder Wall");
        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage ability cannot be activated when Tinder Wall is not blocking")
    void cannotDamageCreatureOutsideCombat() {
        addCreatureReady(player1, new TinderWall());
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Tinder Wall");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    /** Declares player1's first creature as an attacker and blocks it with player2's Tinder Wall. */
    private void blockWithWall() {
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
