package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GaleriderSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarmonicSliver.class, GaleriderSliver.class, GrizzlyBears.class, LeoninScimitar.class, BadMoon.class,
        Forest.class})
class HarmonicSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Harmonic Sliver gets its own enter-the-battlefield trigger")
    void getsItsOwnGrantedTrigger() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.setHand(player1, List.of(new HarmonicSliver()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
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

        harness.setHand(player1, List.of(new GaleriderSliver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
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
        harness.setHand(player2, List.of(new GaleriderSliver()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);
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

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
    }
}
