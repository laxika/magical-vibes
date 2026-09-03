package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MarbleDiamond;
import com.github.laxika.magicalvibes.cards.v.ViashinoWarrior;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianVault.class, ViashinoWarrior.class, MarbleDiamond.class, Forest.class})
class PhyrexianVaultTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PhyrexianVault()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(PhyrexianVault.class);
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new PhyrexianVault()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Phyrexian Vault");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new PhyrexianVault()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Activation: sacrificing a creature and drawing =====

    @Test
    @DisplayName("Activating ability sacrifices creature, taps vault, and puts draw on stack")
    void activatingAbilitySacrificesCreatureAndPutsDrawOnStack() {
        addCreatureReady(player1, new PhyrexianVault());
        harness.addToBattlefield(player1, new ViashinoWarrior());

        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        // Viashino Warrior should be sacrificed
        harness.assertNotOnBattlefield(player1, "Viashino Warrior");
        harness.assertInGraveyard(player1, "Viashino Warrior");

        // Phyrexian Vault should still be on the battlefield
        harness.assertOnBattlefield(player1, "Phyrexian Vault");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(PhyrexianVault.class);
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Activating ability sacrifices the chosen creature when several are available")
    void activatingAbilitySacrificesChosenCreature() {
        addCreatureReady(player1, new PhyrexianVault());
        Permanent firstCreature = addCreatureReady(player1, new ViashinoWarrior());
        Permanent chosenCreature = addCreatureReady(player1, new ViashinoWarrior());

        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handlePermanentChosen(player1, chosenCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(firstCreature.getCard())
                .doesNotContain(chosenCreature.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(chosenCreature.getCard());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Activating ability taps the vault")
    void activatingTapsVault() {
        Permanent vault = addCreatureReady(player1, new PhyrexianVault());
        harness.addToBattlefield(player1, new ViashinoWarrior());

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThat(vault.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);

        assertThat(vault.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addCreatureReady(player1, new PhyrexianVault());
        harness.addToBattlefield(player1, new ViashinoWarrior());

        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving ability draws a card")
    void resolvingDrawsACard() {
        addCreatureReady(player1, new PhyrexianVault());
        harness.addToBattlefield(player1, new ViashinoWarrior());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Resolving ability does not affect opponent's hand")
    void doesNotAffectOpponent() {
        addCreatureReady(player1, new PhyrexianVault());
        harness.addToBattlefield(player1, new ViashinoWarrior());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new ViashinoWarrior()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Vault remains on battlefield after activation and resolution")
    void remainsOnBattlefieldAfterResolution() {
        addCreatureReady(player1, new PhyrexianVault());
        harness.addToBattlefield(player1, new ViashinoWarrior());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phyrexian Vault");
        harness.assertNotInGraveyard(player1, "Phyrexian Vault");
    }

    @Test
    @DisplayName("Drawing from empty deck is handled")
    void drawingFromEmptyDeck() {
        addCreatureReady(player1, new PhyrexianVault());
        harness.addToBattlefield(player1, new ViashinoWarrior());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("no cards to draw")).isTrue();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new PhyrexianVault());
        harness.addToBattlefield(player1, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent vault = addCreatureReady(player1, new PhyrexianVault());
        vault.tap();
        harness.addToBattlefield(player1, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutSacrificeTarget() {
        addCreatureReady(player1, new PhyrexianVault());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when only the opponent controls a creature")
    void cannotActivateWithOnlyOpponentCreature() {
        addCreatureReady(player1, new PhyrexianVault());
        addCreatureReady(player2, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Viashino Warrior");
    }

    @Test
    @DisplayName("Cannot activate with only non-creature permanents (no creatures to sacrifice)")
    void cannotActivateWithOnlyNonCreatures() {
        addCreatureReady(player1, new PhyrexianVault());
        harness.addToBattlefield(player1, new MarbleDiamond());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== No summoning sickness for artifacts =====

    @Test
    @DisplayName("Can activate ability the turn it enters the battlefield (no summoning sickness for artifacts)")
    void noSummoningSicknessForArtifact() {
        Permanent vault = addCreatureReady(player1, new PhyrexianVault());
        vault.setSummoningSick(true);
        harness.addToBattlefield(player1, new ViashinoWarrior());

        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Logging =====

    @Test
    @DisplayName("Sacrificing a creature logs the sacrifice")
    void sacrificingCreatureLogsIt() {
        addCreatureReady(player1, new PhyrexianVault());
        harness.addToBattlefield(player1, new ViashinoWarrior());

        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gameLogContains("sacrifices Viashino Warrior")).isTrue();
    }

    @Test
    @DisplayName("Activating ability logs the activation")
    void activatingAbilityLogsActivation() {
        addCreatureReady(player1, new PhyrexianVault());
        harness.addToBattlefield(player1, new ViashinoWarrior());

        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gameLogContains("activates Phyrexian Vault's ability")).isTrue();
    }

    @Test
    @DisplayName("Resolving ability logs the card draw")
    void resolvingLogsCardDraw() {
        addCreatureReady(player1, new PhyrexianVault());
        harness.addToBattlefield(player1, new ViashinoWarrior());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gameLogContains("draws a card")).isTrue();
    }
}

