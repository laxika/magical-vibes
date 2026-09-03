package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MysticRemora;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcyManipulator.class, BalduvianBears.class, Forest.class, MysticRemora.class, ZuranOrb.class})
class IcyManipulatorTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        IcyManipulator card = new IcyManipulator();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard()).isSameAs(card);
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        IcyManipulator card = new IcyManipulator();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card);
    }

    // ===== Activated ability: targeting creatures =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingTargetingCreaturePutsOnStack() {
        Permanent icy = addReadyIcy(player1);
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isSameAs(icy.getCard());
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Icy Manipulator")
    void activatingTapsIcy() {
        Permanent icy = addReadyIcy(player1);
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(icy.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability taps target creature")
    void resolvingTapsTargetCreature() {
        addReadyIcy(player1);
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    // ===== Activated ability: targeting lands =====

    @Test
    @DisplayName("Can tap target land")
    void canTapTargetLand() {
        addReadyIcy(player1);
        Permanent targetLand = addReadyLand(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        assertThat(targetLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap own land")
    void canTapOwnLand() {
        Permanent icy = addReadyIcy(player1);
        Permanent ownLand = addReadyLand(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Icy is at index 0, land is at index 1
        harness.activateAbility(player1, 0, null, ownLand.getId());
        harness.passBothPriorities();

        assertThat(ownLand.isTapped()).isTrue();
    }

    // ===== Activated ability: targeting artifacts =====

    @Test
    @DisplayName("Can tap target artifact")
    void canTapTargetArtifact() {
        addReadyIcy(player1);
        Permanent targetArtifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        assertThat(targetArtifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target an already tapped permanent")
    void canTargetAlreadyTappedPermanent() {
        addReadyIcy(player1);
        Permanent targetLand = addReadyLand(player2);
        targetLand.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        assertThat(targetLand.isTapped()).isTrue();
    }

    // ===== Activated ability: targeting enchantments (invalid) =====

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addReadyIcy(player1);
        Permanent enchantment = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    // ===== No summoning sickness for artifacts =====

    @Test
    @DisplayName("Can activate ability the turn it enters the battlefield (no summoning sickness for artifacts)")
    void noSummoningSicknessForArtifact() {
        IcyManipulator card = new IcyManipulator();
        Permanent icy = harness.addToBattlefieldAndReturn(player1, card);
        // Do NOT clear summoning sickness — artifacts should be able to use tap abilities regardless
        icy.setSummoningSick(true);

        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Should not throw — artifacts ignore summoning sickness
        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(icy.isTapped()).isTrue();
    }

    // ===== Mana cost =====

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addReadyIcy(player1);
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyIcy(player1);
        Permanent target = addCreatureReady(player2, new BalduvianBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Cannot activate when tapped =====

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent icy = addReadyIcy(player1);
        icy.tap();
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyIcy(player1);
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    // ===== Can target own permanents =====

    @Test
    @DisplayName("Can tap own creature")
    void canTapOwnCreature() {
        addReadyIcy(player1);
        Permanent ownCreature = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
    }

    // ===== Game log =====

    @Test
    @DisplayName("Resolving ability adds to game log")
    void resolvingAddsToGameLog() {
        addReadyIcy(player1);
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains("taps")).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyIcy(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new IcyManipulator());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ZuranOrb());
    }

    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MysticRemora());
    }
}

