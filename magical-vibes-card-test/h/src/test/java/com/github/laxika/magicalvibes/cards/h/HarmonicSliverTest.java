package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GaleriderSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarmonicSliver.class, GaleriderSliver.class, GrizzlyBears.class, LeoninScimitar.class, BadMoon.class,
        Forest.class})
class HarmonicSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Harmonic Sliver gets its own enter-the-battlefield trigger")
    void getsItsOwnGrantedTrigger() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.castFromHand(player1, new HarmonicSliver(), "{1}{G}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("A Sliver entering under your control destroys an artifact")
    void sliverEnteringUnderYourControlDestroysArtifact() {
        harness.addToBattlefield(player1, new HarmonicSliver());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.castFromHand(player1, new GaleriderSliver(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(artifact.getId());
        assertThat(choice.validIds()).doesNotContain(land.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("An opposing Sliver also gets Harmonic Sliver's trigger")
    void opposingSliverGetsGrantedTrigger() {
        harness.addToBattlefield(player1, new HarmonicSliver());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BadMoon());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GaleriderSliver(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, enchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not get Harmonic Sliver's trigger")
    void nonSliverDoesNotGetGrantedTrigger() {
        harness.addToBattlefield(player1, new HarmonicSliver());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
    }

    @Test
    @DisplayName("The mandatory trigger destroys your own artifact when it is the only legal target")
    void mustDestroyOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        harness.castFromHand(player1, new HarmonicSliver(), "{1}{G}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Harmonic Sliver enters normally without any legal target")
    void entersWithoutLegalTarget() {
        harness.castFromHand(player1, new HarmonicSliver(), "{1}{G}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Harmonic Sliver");
    }

    @Test
    @DisplayName("A second Harmonic Sliver has two independent triggers with different targets")
    void multipleHarmonicSliversGrantIndependentTriggers() {
        harness.addToBattlefield(player1, new HarmonicSliver());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.castFromHand(player1, new HarmonicSliver(), "{1}{G}{W}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, firstArtifact.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondArtifact.getId());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(firstArtifact, secondArtifact);
    }
}
