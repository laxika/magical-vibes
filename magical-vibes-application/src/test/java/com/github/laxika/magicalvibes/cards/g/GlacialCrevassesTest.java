package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlacialCrevasses.class, BalduvianBears.class, Incinerate.class, Mountain.class,
        SnowCoveredMountain.class})
class GlacialCrevassesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a snow Mountain pays the activation cost")
    void sacrificingSnowMountainPaysActivationCost() {
        addCrevasses(player1);
        Permanent snowMountain = addSnowMountain(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(snowMountain);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(snowMountain.getCard().getId()));
    }

    @Test
    @DisplayName("An unblocked attacker deals no combat damage after the ability resolves")
    void unblockedAttackerDealsNoDamage() {
        harness.setLife(player1, 20);
        addCrevasses(player1);
        addSnowMountain(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new BalduvianBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot activate with only a nonsnow Mountain")
    void cannotActivateWithoutSnowMountain() {
        addCrevasses(player1);
        addMountain(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate using a snow Mountain controlled by another player")
    void cannotActivateUsingOpponentSnowMountain() {
        addCrevasses(player1);
        addSnowMountain(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents combat damage dealt to and by creatures in combat")
    void preventsCombatDamageToCreatures() {
        addCrevasses(player1);
        addSnowMountain(player1);
        Permanent blocker = addCreatureReady(player1, new BalduvianBears());
        Permanent attacker = addCreatureReady(player2, new BalduvianBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        prepareDeclareBlockers(player2);
        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        harness.setLife(player2, 20);
        addCrevasses(player1);
        addSnowMountain(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private Permanent addCrevasses(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GlacialCrevasses());
    }

    private Permanent addSnowMountain(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SnowCoveredMountain());
    }

    private Permanent addMountain(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Mountain());
    }
}
