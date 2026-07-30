package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscipleOfBolasTest extends BaseCardTest {

    /** Casts Disciple of Bolas and resolves it so the enter trigger is done resolving. */
    private void castDisciple() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new DiscipleOfBolas()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> enters, ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger -> sacrifice prompt
    }

    @Test
    @DisplayName("Sacrificing a creature gains life and draws cards equal to its power")
    void sacrificeGainsLifeAndDrawsEqualToPower() {
        harness.addToBattlefield(player1, new HillGiant()); // 3/3
        int lifeBefore = gd.getLife(player1.getId());

        castDisciple();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Hill Giant"));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("X is the sacrificed creature's power, not the Disciple's")
    void xTracksSacrificedCreaturePower() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        int lifeBefore = gd.getLife(player1.getId());

        castDisciple();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Sacrificing a 0-power creature gains no life and draws nothing")
    void zeroPowerGainsNothing() {
        harness.addToBattlefield(player1, new Ornithopter()); // 0/2
        int lifeBefore = gd.getLife(player1.getId());

        castDisciple();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Ornithopter"));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertNotOnBattlefield(player1, "Ornithopter");
    }

    @Test
    @DisplayName("With no other creature nothing happens and the Disciple survives")
    void noOtherCreatureDoesNothing() {
        int lifeBefore = gd.getLife(player1.getId());

        castDisciple();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertOnBattlefield(player1, "Disciple of Bolas");
    }

    @Test
    @DisplayName("The Disciple itself is not a legal sacrifice choice")
    void discipleCannotSacrificeItself() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castDisciple();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .containsExactly(harness.getPermanentId(player1, "Grizzly Bears"));
    }

    @Test
    @DisplayName("An opponent's creature is not a legal sacrifice choice")
    void opponentCreatureIsNotChoosable() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        castDisciple();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .doesNotContain(harness.getPermanentId(player2, "Hill Giant"));
    }
}
