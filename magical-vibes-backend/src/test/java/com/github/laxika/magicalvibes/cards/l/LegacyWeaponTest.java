package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegacyWeaponTest {

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
    @DisplayName("Legacy Weapon has correct card properties")
    void hasCorrectProperties() {
        LegacyWeapon card = new LegacyWeapon();

        assertThat(card.getName()).isEqualTo("Legacy Weapon");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{7}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getPower()).isNull();
        assertThat(card.getToughness()).isNull();
        assertThat(card.isShufflesIntoLibraryFromGraveyard()).isTrue();
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{W}{U}{B}{R}{G}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(ExileTargetPermanentEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack as an artifact spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new LegacyWeapon()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Legacy Weapon");
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new LegacyWeapon()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Legacy Weapon"));
    }

    // ===== Activated ability: exile creature =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingTargetingCreaturePutsOnStack() {
        addReadyLegacyWeapon(player1);
        Permanent target = addReadyCreature(player2);
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Legacy Weapon");
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability exiles target creature")
    void resolvingExilesTargetCreature() {
        addReadyLegacyWeapon(player1);
        Permanent target = addReadyCreature(player2);
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature is no longer on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Creature is in exile zone
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Creature is NOT in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Activated ability: exile land =====

    @Test
    @DisplayName("Can exile target land")
    void canExileTargetLand() {
        addReadyLegacyWeapon(player1);
        Permanent targetLand = addReadyLand(player2);
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    // ===== Activated ability: exile artifact =====

    @Test
    @DisplayName("Can exile target artifact")
    void canExileTargetArtifact() {
        addReadyLegacyWeapon(player1);
        Permanent targetArtifact = addReadyArtifact(player2);
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angel's Feather"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angel's Feather"));
    }

    // ===== Activated ability: exile enchantment =====

    @Test
    @DisplayName("Can exile target enchantment")
    void canExileTargetEnchantment() {
        addReadyLegacyWeapon(player1);
        Permanent targetEnchantment = addReadyEnchantment(player2);
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, targetEnchantment.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pacifism"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Pacifism"));
    }

    // ===== Mana cost =====

    @Test
    @DisplayName("Consumes WUBRG mana when activating ability")
    void manaIsConsumed() {
        addReadyLegacyWeapon(player1);
        Permanent target = addReadyCreature(player2);
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyLegacyWeapon(player1);
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate with only partial WUBRG mana")
    void cannotActivateWithPartialMana() {
        addReadyLegacyWeapon(player1);
        Permanent target = addReadyCreature(player2);
        // Only add 4 of the 5 colors
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== No tap required =====

    @Test
    @DisplayName("Does not tap when activating ability")
    void doesNotTapWhenActivating() {
        Permanent weapon = addReadyLegacyWeapon(player1);
        Permanent target = addReadyCreature(player2);
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(weapon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability even when tapped")
    void canActivateWhenTapped() {
        Permanent weapon = addReadyLegacyWeapon(player1);
        weapon.tap();
        Permanent target = addReadyCreature(player2);
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability multiple times if enough mana")
    void canActivateMultipleTimes() {
        addReadyLegacyWeapon(player1);
        Permanent target1 = addReadyCreature(player2);
        Permanent target2 = addReadyLand(player2);
        // Add mana for two activations
        addWubrgMana(player1);
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target1.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, target2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyLegacyWeapon(player1);
        Permanent target = addReadyCreature(player2);
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerExiledCards.get(player2.getId())).isEmpty();
    }

    // ===== Replacement effect: destroyed =====

    @Test
    @DisplayName("When destroyed, Legacy Weapon is shuffled into library instead of graveyard")
    void replacementEffectOnDestruction() {
        harness.addToBattlefield(player2, new LegacyWeapon());
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        UUID targetId = harness.getPermanentId(player2, "Legacy Weapon");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Not on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Legacy Weapon"));
        // NOT in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Legacy Weapon"));
        // Shuffled into library
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Legacy Weapon"));
        // Log confirms replacement
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Legacy Weapon") && log.contains("shuffled into its owner's library instead"));
    }

    // ===== Can exile own permanents =====

    @Test
    @DisplayName("Can exile own permanent")
    void canExileOwnPermanent() {
        addReadyLegacyWeapon(player1);
        Permanent ownCreature = addReadyCreature(player1);
        addWubrgMana(player1);

        // Legacy Weapon is at index 0, creature at index 1
        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Game log =====

    @Test
    @DisplayName("Resolving exile ability adds to game log")
    void resolvingAddsToGameLog() {
        addReadyLegacyWeapon(player1);
        Permanent target = addReadyCreature(player2);
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Grizzly Bears") && log.contains("exiled"));
    }

    // ===== Helpers =====

    private Permanent addReadyLegacyWeapon(Player player) {
        LegacyWeapon card = new LegacyWeapon();
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

    private void addWubrgMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}

