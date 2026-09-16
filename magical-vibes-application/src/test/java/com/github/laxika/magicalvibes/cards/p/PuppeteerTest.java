package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.e.EmberBeast;
import com.github.laxika.magicalvibes.cards.m.MossfireEgg;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Puppeteer.class, EmberBeast.class, MossfireEgg.class})
class PuppeteerTest extends BaseCardTest {

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingPutsOnStack() {
        Permanent puppeteer = addReadyPuppeteer(player1);
        Permanent target = addCreatureReady(player2, new EmberBeast());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Puppeteer")
    void activatingTapsPuppeteer() {
        Permanent puppeteer = addReadyPuppeteer(player1);
        Permanent target = addCreatureReady(player2, new EmberBeast());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(puppeteer.isTapped()).isTrue();
    }

    // ===== Tapping untapped creatures =====

    @Test
    @DisplayName("Taps an untapped creature")
    void tapsUntappedCreature() {
        addReadyPuppeteer(player1);
        Permanent target = addCreatureReady(player2, new EmberBeast());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(target.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    // ===== Untapping tapped creatures =====

    @Test
    @DisplayName("Untaps a tapped creature")
    void untapsTappedCreature() {
        addReadyPuppeteer(player1);
        Permanent target = addCreatureReady(player2, new EmberBeast());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(target.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    // ===== Targeting own creatures =====

    @Test
    @DisplayName("Can tap own untapped creature")
    void canTapOwnCreature() {
        addReadyPuppeteer(player1);
        Permanent ownCreature = addCreatureReady(player1, new EmberBeast());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can untap own tapped creature")
    void canUntapOwnCreature() {
        addReadyPuppeteer(player1);
        Permanent ownCreature = addCreatureReady(player1, new EmberBeast());
        ownCreature.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownCreature.isTapped()).isFalse();
    }

    // ===== Invalid targets =====

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyPuppeteer(player1);
        Permanent artifactPerm = harness.addToBattlefieldAndReturn(player2, new MossfireEgg());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifactPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Costs =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyPuppeteer(player1);
        Permanent target = addCreatureReady(player2, new EmberBeast());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent puppeteer = addReadyPuppeteer(player1);
        puppeteer.tap();
        Permanent target = addCreatureReady(player2, new EmberBeast());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Puppeteer card = new Puppeteer();
        Permanent puppeteer = new Permanent(card);
        // summoningSick is true by default
        gd.playerBattlefields.get(player1.getId()).add(puppeteer);
        Permanent target = addCreatureReady(player2, new EmberBeast());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyPuppeteer(player1);
        Permanent target = addCreatureReady(player2, new EmberBeast());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can decline tapping or untapping a target creature")
    void canDeclineTapOrUntap() {
        addReadyPuppeteer(player1);
        Permanent target = addCreatureReady(player2, new EmberBeast());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReadyPuppeteer(Player player) {
        return addCreatureReady(player, new Puppeteer());
    }
}
