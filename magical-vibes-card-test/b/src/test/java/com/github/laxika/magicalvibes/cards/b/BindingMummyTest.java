package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BindingMummy.class, AngelsFeather.class, Forest.class, GrizzlyBears.class, ScatheZombies.class})
class BindingMummyTest extends BaseCardTest {

    @Test
    @DisplayName("Another Zombie entering lets the controller tap target creature")
    void zombieEnterTapsTargetCreature() {
        harness.addToBattlefield(player1, new BindingMummy());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castScatheZombies(player1);
        harness.passBothPriorities(); // resolve the creature — Binding Mummy triggers and asks for a target
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tap target may be an artifact")
    void zombieEnterTapsTargetArtifact() {
        harness.addToBattlefield(player1, new BindingMummy());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());

        castScatheZombies(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the trigger taps nothing")
    void declineTapsNothing() {
        harness.addToBattlefield(player1, new BindingMummy());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castScatheZombies(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(victim.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A non-Zombie creature entering does not trigger the tap ability")
    void nonZombieEnterDoesNotTrigger() {
        harness.addToBattlefield(player1, new BindingMummy());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities(); // resolve the creature

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The tap ability cannot target a land (artifact or creature only)")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new BindingMummy());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        castScatheZombies(player1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    private void castScatheZombies(Player player) {
        harness.castFromHand(player, new ScatheZombies(), "{2}{B}");
    }

    @Test
    @DisplayName("Binding Mummy does not trigger for its own entry")
    void ownEntryDoesNotTrigger() {
        harness.castFromHand(player1, new BindingMummy(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's Zombie entering does not trigger Binding Mummy")
    void opponentsZombieDoesNotTrigger() {
        harness.addToBattlefield(player1, new BindingMummy());

        harness.enterBattlefieldAndReturn(player2, new ScatheZombies());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Binding Mummy may target itself")
    void canTapItself() {
        Permanent mummy = harness.addToBattlefieldAndReturn(player1, new BindingMummy());

        castScatheZombies(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, mummy.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(mummy.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An already tapped creature remains a legal target")
    void canTargetTappedCreature() {
        harness.addToBattlefield(player1, new BindingMummy());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        victim.tap();

        castScatheZombies(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(victim.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
