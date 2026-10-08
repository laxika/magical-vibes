package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({VedalkenAnatomist.class, GrizzlyBears.class, LlanowarElves.class, AngelsFeather.class})
class VedalkenAnatomistTest extends BaseCardTest {

    @Test
    @DisplayName("Can target itself and untap after paying the activation costs")
    void canTargetItselfAndUntap() {
        Permanent anatomist = addReadyAnatomist(player1);
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, anatomist.getId());

        assertThat(anatomist.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(anatomist.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(anatomist.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingPutsOnStack() {
        addReadyAnatomist(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(VedalkenAnatomist.class);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Vedalken Anatomist")
    void activatingTapsAnatomist() {
        Permanent anatomist = addReadyAnatomist(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(anatomist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Puts a -1/-1 counter on target creature and prompts may choice")
    void putsCounterAndPromptsMayChoice() {
        addReadyAnatomist(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // -1/-1 counter should be placed
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        // May ability should be pending
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may choice taps an untapped target creature")
    void acceptingMayTapsUntappedCreature() {
        addReadyAnatomist(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAnatomistMana(player1);

        assertThat(target.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting may choice untaps a tapped target creature")
    void acceptingMayUntapsTappedCreature() {
        addReadyAnatomist(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        addAnatomistMana(player1);

        assertThat(target.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining may choice leaves untapped creature untapped")
    void decliningMayLeavesCreatureUntapped() {
        addReadyAnatomist(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAnatomistMana(player1);

        assertThat(target.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // Counter should still be placed
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        // Creature should remain untapped
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining may choice leaves tapped creature tapped")
    void decliningMayLeavesCreatureTapped() {
        addReadyAnatomist(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Kills a 1/1 creature after the may choice finishes resolving")
    void killsOneOneCreature() {
        addReadyAnatomist(player1);
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        addReadyAnatomist(player1);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyAnatomist(player1);
        Permanent artifactPerm = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        addAnatomistMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifactPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyAnatomist(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent anatomist = addReadyAnatomist(player1);
        anatomist.tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAnatomistMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new VedalkenAnatomist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAnatomistMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyAnatomist(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Ability still resolves when its source leaves the battlefield")
    void resolvesWithoutItsSource() {
        Permanent anatomist = addReadyAnatomist(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAnatomistMana(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(anatomist);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature with zero toughness can still be untapped before it dies")
    void untapsBeforeStateBasedDeath() {
        addReadyAnatomist(player1);
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        target.tap();
        addAnatomistMana(player1);
        harness.activateAbility(player1, 0, null, target.getId());

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(gqs.getEffectiveToughness(gd, target)).isZero();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyAnatomist(Player player) {
        return addCreatureReady(player, new VedalkenAnatomist());
    }

    private void addAnatomistMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
