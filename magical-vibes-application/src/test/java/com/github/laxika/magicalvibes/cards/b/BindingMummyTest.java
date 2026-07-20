package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BindingMummyTest extends BaseCardTest {

    @Test
    @DisplayName("Another Zombie entering lets the controller tap target creature")
    void zombieEnterTapsTargetCreature() {
        harness.addToBattlefield(player1, new BindingMummy());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castScatheZombies(player1);
        harness.passBothPriorities(); // resolve the creature — Binding Mummy triggers
        harness.passBothPriorities(); // resolve the MayEffect → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, victim.getId());

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tap target may be an artifact")
    void zombieEnterTapsTargetArtifact() {
        harness.addToBattlefield(player1, new BindingMummy());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());

        castScatheZombies(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the trigger taps nothing")
    void declineTapsNothing() {
        harness.addToBattlefield(player1, new BindingMummy());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castScatheZombies(player1);
        harness.passBothPriorities();
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

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The tap ability cannot target a land (artifact or creature only)")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new BindingMummy());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        castScatheZombies(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castScatheZombies(Player player) {
        harness.setHand(player, List.of(new ScatheZombies()));
        harness.addMana(player, ManaColor.BLACK, 3);
        harness.castCreature(player, 0);
    }
}
