package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({Sunstone.class, BalduvianBears.class, Incinerate.class, Mountain.class,
        SnowCoveredMountain.class})
class SunstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} and sacrificing a snow land sacrifices that land")
    void sacrificesSnowLandWhenActivated() {
        addSunstone(player1);
        Permanent snowLand = addLand(player1, true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(snowLand);
    }

    @Test
    @DisplayName("An unblocked attacker deals no combat damage after the ability resolves")
    void unblockedAttackerDealsNoDamage() {
        harness.setLife(player1, 20);
        addSunstone(player1);
        addLand(player1, true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        addCreatureReady(player2, new BalduvianBears());
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents combat damage dealt to attacking and blocking creatures")
    void preventsCombatDamageToCreatures() {
        addSunstone(player1);
        addLand(player1, true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        harness.setLife(player1, 20);
        addSunstone(player1);
        addLand(player1, true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot activate with only a nonsnow land")
    void cannotActivateWithoutSnowLand() {
        addSunstone(player1);
        addLand(player1, false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped Sunstone can sacrifice a tapped snow land")
    void canActivateWithTappedSourceAndTappedSnowLand() {
        harness.setLife(player1, 20);
        Permanent sunstone = harness.addToBattlefieldAndReturn(player1, new Sunstone());
        sunstone.tap();
        Permanent snowLand = addLand(player1, true);
        snowLand.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(snowLand);
        harness.assertInGraveyard(player1, "Snow-Covered Mountain");
        harness.passBothPriorities();

        addCreatureReady(player2, new BalduvianBears());
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sunstone);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's snow land to activate Sunstone")
    void cannotActivateUsingOpponentsSnowLand() {
        addSunstone(player1);
        addLand(player2, true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without two mana")
    void cannotActivateWithoutEnoughMana() {
        addSunstone(player1);
        Permanent snowLand = addLand(player1, true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(snowLand);
    }

    private void addSunstone(Player player) {
        harness.addToBattlefield(player, new Sunstone());
    }

    private Permanent addLand(Player player, boolean snow) {
        return harness.addToBattlefieldAndReturn(player,
                snow ? new SnowCoveredMountain() : new Mountain());
    }
}
