package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IcyManipulatorTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Icy Manipulator has correct card properties")
    void hasCorrectProperties() {
        IcyManipulator card = new IcyManipulator();

        assertThat(card.getName()).isEqualTo("Icy Manipulator");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{4}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getPower()).isNull();
        assertThat(card.getToughness()).isNull();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getCardText()).isEqualTo("{1}, {T}: Tap target artifact, creature, or land.");
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(TapTargetPermanentEffect.class);
        TapTargetPermanentEffect effect = (TapTargetPermanentEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(effect.allowedTypes()).containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE, CardType.BASIC_LAND);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}");
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new IcyManipulator()));
        harness.addMana(player1, "W", 4);

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Icy Manipulator");
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new IcyManipulator()));
        harness.addMana(player1, "W", 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Icy Manipulator"));
    }

    // ===== Activated ability: targeting creatures =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingTargetingCreaturePutsOnStack() {
        Permanent icy = addReadyIcy(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Icy Manipulator");
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Icy Manipulator")
    void activatingTapsIcy() {
        Permanent icy = addReadyIcy(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(icy.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability taps target creature")
    void resolvingTapsTargetCreature() {
        addReadyIcy(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, "W", 1);

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
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        assertThat(targetLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap own land")
    void canTapOwnLand() {
        Permanent icy = addReadyIcy(player1);
        Permanent ownLand = addReadyLand(player1);
        harness.addMana(player1, "W", 1);

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
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        assertThat(targetArtifact.isTapped()).isTrue();
    }

    // ===== Activated ability: targeting enchantments (invalid) =====

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addReadyIcy(player1);
        Permanent enchantment = addReadyEnchantment(player2);
        harness.addMana(player1, "W", 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    // ===== No summoning sickness for artifacts =====

    @Test
    @DisplayName("Can activate ability the turn it enters the battlefield (no summoning sickness for artifacts)")
    void noSummoningSicknessForArtifact() {
        IcyManipulator card = new IcyManipulator();
        Permanent icy = new Permanent(card);
        // Do NOT clear summoning sickness — artifacts should be able to use tap abilities regardless
        icy.setSummoningSick(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(icy);

        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, "W", 1);

        // Should not throw — artifacts ignore summoning sickness
        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(icy.isTapped()).isTrue();
    }

    // ===== Mana cost =====

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addReadyIcy(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, "W", 2);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyIcy(player1);
        Permanent target = addReadyCreature(player2);

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
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, "W", 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyIcy(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Can target own permanents =====

    @Test
    @DisplayName("Can tap own creature")
    void canTapOwnCreature() {
        addReadyIcy(player1);
        Permanent ownCreature = addReadyCreature(player1);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
    }

    // ===== Game log =====

    @Test
    @DisplayName("Resolving ability adds to game log")
    void resolvingAddsToGameLog() {
        addReadyIcy(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Icy Manipulator") && log.contains("taps") && log.contains("Grizzly Bears"));
    }

    // ===== Helpers =====

    private Permanent addReadyIcy(Player player) {
        IcyManipulator card = new IcyManipulator();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyLand(Player player) {
        Forest card = new Forest();
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyArtifact(Player player) {
        AngelsFeather card = new AngelsFeather();
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyEnchantment(Player player) {
        Pacifism card = new Pacifism();
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
