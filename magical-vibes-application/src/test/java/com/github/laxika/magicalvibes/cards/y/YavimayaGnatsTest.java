package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YavimayaGnats.class, BalduvianBears.class})
class YavimayaGnatsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration puts the ability on the stack for Yavimaya Gnats")
    void activatingRegenPutsOnStackForSource() {
        Permanent perm = addCreatureReady(player1, new YavimayaGnats());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addCreatureReady(player1, new YavimayaGnats());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent gnats = findPermanent(player1, "Yavimaya Gnats");
        assertThat(gnats.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Yavimaya Gnats from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent perm = addCreatureReady(player1, new YavimayaGnats());
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Yavimaya Gnats");
        Permanent gnats = findPermanent(player1, "Yavimaya Gnats");
        assertThat(gnats.isTapped()).isTrue();
        assertThat(gnats.isBlocking()).isFalse();
        assertThat(gnats.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Yavimaya Gnats dies without a regeneration shield")
    void diesWithoutRegenShield() {
        Permanent perm = addCreatureReady(player1, new YavimayaGnats());
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Yavimaya Gnats");
        harness.assertInGraveyard(player1, "Yavimaya Gnats");
    }

    @Test
    @DisplayName("Regeneration can be activated while tapped and summoning sick")
    void regeneratesWhileTappedAndSummoningSick() {
        Permanent gnats = harness.addToBattlefieldAndReturn(player1, new YavimayaGnats());
        gnats.setSummoningSick(true);
        gnats.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();

        assertThat(gnats.getRegenerationShield()).isEqualTo(1);
        assertThat(gnats.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each activation creates a shield only on its source without tapping it")
    void repeatedActivationsShieldOnlySource() {
        Permanent gnats = addCreatureReady(player1, new YavimayaGnats());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gnats.getRegenerationShield()).isEqualTo(2);
        assertThat(gnats.isTapped()).isFalse();
        assertThat(bears.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("An activated regeneration shield saves the source and removes its damage")
    void activatedShieldSavesFromCombatDamage() {
        Permanent gnats = addCreatureReady(player1, new YavimayaGnats());
        Permanent attacker = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gnats.setBlocking(true);
        gnats.addBlockingTarget(0);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Yavimaya Gnats");
        harness.assertNotInGraveyard(player1, "Yavimaya Gnats");
        assertThat(gnats.getRegenerationShield()).isZero();
        assertThat(gnats.getMarkedDamage()).isZero();
        assertThat(gnats.isTapped()).isTrue();
        assertThat(gnats.isBlocking()).isFalse();
        assertThat(gnats.getBlockingTargets()).isEmpty();
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Yavimaya Gnats")
    void nonFlyingCreatureCannotBlock() {
        addCreatureReady(player1, new YavimayaGnats());
        addCreatureReady(player2, new BalduvianBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("Colorless mana cannot pay the regeneration cost")
    void regenerationRequiresGreenMana() {
        Permanent gnats = addCreatureReady(player1, new YavimayaGnats());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gnats.getRegenerationShield()).isZero();
        assertThat(gnats.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each shield replaces only one destruction, including while already tapped")
    void multipleShieldsSaveFromSeparateLethalDamageEvents() {
        Permanent gnats = addCreatureReady(player1, new YavimayaGnats());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gnats.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Yavimaya Gnats");
        assertThat(gnats.getRegenerationShield()).isEqualTo(1);
        assertThat(gnats.getMarkedDamage()).isZero();
        assertThat(gnats.isTapped()).isTrue();

        gnats.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Yavimaya Gnats");
        assertThat(gnats.getRegenerationShield()).isZero();
        assertThat(gnats.getMarkedDamage()).isZero();

        gnats.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Yavimaya Gnats");
        harness.assertInGraveyard(player1, "Yavimaya Gnats");
    }

    @Test
    @DisplayName("Regeneration cannot save a creature with zero toughness")
    void regenerationDoesNotPreventZeroToughnessDeath() {
        Permanent gnats = addCreatureReady(player1, new YavimayaGnats());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gnats.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Yavimaya Gnats");
        harness.assertInGraveyard(player1, "Yavimaya Gnats");
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        BalduvianBears card = new BalduvianBears();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }
}
