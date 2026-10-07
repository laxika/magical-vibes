package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.o.OrcishOriflamme;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Twiddle.class, GrizzlyBears.class, Forest.class, HowlingMine.class, OrcishOriflamme.class})
class TwiddleTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped creature")
    void tapsUntappedCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(target.isTapped()).isFalse();

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps an untapped land")
    void tapsUntappedLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps an untapped artifact")
    void tapsUntappedArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HowlingMine());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a tapped land")
    void untapsTappedLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.tap();
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(target.isTapped()).isTrue();

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addCreatureReady(player1, new GrizzlyBears()); // valid target so spell is playable
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new OrcishOriflamme());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    @Test
    void waitsForResolutionChoice() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canDeclineTapOrUntap() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not automatically untap a target tapped before resolution")
    void doesNotAutomaticallyUntapTargetTappedBeforeResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        target.tap();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can choose to untap a target tapped before resolution")
    void canChooseToUntapTargetTappedBeforeResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        target.tap();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not resolve its effect when the target leaves the battlefield")
    void doesNotResolveWhenTargetLeavesBattlefield() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Twiddle");
    }

    @Test
    @DisplayName("Twiddle goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Twiddle");
    }

    @Test
    @DisplayName("Can untap own tapped creature")
    void canUntapOwnCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.tap();
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, ownCreature.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps a tapped artifact")
    void untapsTappedArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HowlingMine());
        target.tap();
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Twiddle");
    }

    @Test
    @DisplayName("Can decline to untap a tapped artifact")
    void canDeclineToUntapTappedArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HowlingMine());
        target.tap();
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Twiddle");
    }

    @Test
    @DisplayName("Can tap a target that became untapped before resolution")
    void canTapTargetUntappedBeforeResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isFalse();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
