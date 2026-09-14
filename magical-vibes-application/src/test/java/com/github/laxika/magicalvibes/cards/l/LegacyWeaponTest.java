package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.GameData;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LegacyWeapon.class, AngelsFeather.class, Demolish.class, Forest.class,
        GloriousAnthem.class, GrizzlyBears.class, MindRot.class})
class LegacyWeaponTest extends BaseCardTest {

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
        harness.assertOnBattlefield(player1, "Legacy Weapon");
    }

    // ===== Activated ability: exile creature =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingTargetingCreaturePutsOnStack() {
        harness.addToBattlefield(player1, new LegacyWeapon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Legacy Weapon");
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability exiles target creature")
    void resolvingExilesTargetCreature() {
        harness.addToBattlefield(player1, new LegacyWeapon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature is no longer on battlefield
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Creature is in exile zone
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Creature is NOT in graveyard
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    // ===== Activated ability: exile land =====

    @Test
    @DisplayName("Can exile target land")
    void canExileTargetLand() {
        harness.addToBattlefield(player1, new LegacyWeapon());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    // ===== Activated ability: exile artifact =====

    @Test
    @DisplayName("Can exile target artifact")
    void canExileTargetArtifact() {
        harness.addToBattlefield(player1, new LegacyWeapon());
        Permanent targetArtifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Angel's Feather");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angel's Feather"));
    }

    // ===== Activated ability: exile enchantment =====

    @Test
    @DisplayName("Can exile target enchantment")
    void canExileTargetEnchantment() {
        harness.addToBattlefield(player1, new LegacyWeapon());
        Permanent targetEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, targetEnchantment.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Glorious Anthem"));
    }

    // ===== Mana cost =====

    @Test
    @DisplayName("Consumes WUBRG mana when activating ability")
    void manaIsConsumed() {
        harness.addToBattlefield(player1, new LegacyWeapon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new LegacyWeapon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate with only partial WUBRG mana")
    void cannotActivateWithPartialMana() {
        harness.addToBattlefield(player1, new LegacyWeapon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent weapon = harness.addToBattlefieldAndReturn(player1, new LegacyWeapon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(weapon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability even when tapped")
    void canActivateWhenTapped() {
        Permanent weapon = harness.addToBattlefieldAndReturn(player1, new LegacyWeapon());
        weapon.tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability multiple times if enough mana")
    void canActivateMultipleTimes() {
        harness.addToBattlefield(player1, new LegacyWeapon());
        Permanent target1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent target2 = harness.addToBattlefieldAndReturn(player2, new Forest());
        // Add mana for two activations
        addWubrgMana(player1);
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target1.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, target2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new LegacyWeapon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
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
        harness.assertNotOnBattlefield(player2, "Legacy Weapon");
        // NOT in graveyard
        harness.assertNotInGraveyard(player2, "Legacy Weapon");
        // Shuffled into library
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Legacy Weapon"));
        // Log confirms replacement
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Legacy Weapon") && log.contains("shuffled into its owner's library instead"));
    }

    @Test
    @DisplayName("When discarded, Legacy Weapon is shuffled into its owner's library instead of the graveyard")
    void replacementEffectFromHand() {
        LegacyWeapon weapon = new LegacyWeapon();
        Forest discardedCard = new Forest();
        Forest remainingCard = new Forest();
        harness.setHand(player1, List.of(new MindRot()));
        harness.setHand(player2, List.of(weapon, discardedCard, remainingCard));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard);
        harness.assertNotInGraveyard(player2, "Legacy Weapon");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId())).contains(weapon);
    }

    // ===== Can exile own permanents =====

    @Test
    @DisplayName("Can exile own permanent")
    void canExileOwnPermanent() {
        harness.addToBattlefield(player1, new LegacyWeapon());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addWubrgMana(player1);

        // Legacy Weapon is at index 0, creature at index 1
        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Game log =====

    @Test
    @DisplayName("Resolving exile ability adds to game log")
    void resolvingAddsToGameLog() {
        harness.addToBattlefield(player1, new LegacyWeapon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addWubrgMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Grizzly Bears") && log.contains("exiled"));
    }

    private void addWubrgMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}

