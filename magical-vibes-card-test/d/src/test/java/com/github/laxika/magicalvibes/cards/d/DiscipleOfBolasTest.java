package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.a.AlexiosDeimosOfKosmos;
import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiscipleOfBolas.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        Ornithopter.class, Cloudshift.class, AlexiosDeimosOfKosmos.class})
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

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("X is the sacrificed creature's power, not the Disciple's")
    void xTracksSacrificedCreaturePower() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        int lifeBefore = gd.getLife(player1.getId());

        castDisciple();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Sacrificing a 0-power creature gains no life and draws nothing")
    void zeroPowerGainsNothing() {
        harness.addToBattlefield(player1, new Ornithopter()); // 0/2
        int lifeBefore = gd.getLife(player1.getId());

        castDisciple();
        int handBefore = gd.playerHands.get(player1.getId()).size();

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
        harness.addToBattlefield(player1, new Ornithopter());

        castDisciple();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                harness.getPermanentId(player1, "Grizzly Bears"), harness.getPermanentId(player1, "Ornithopter"));
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature is not a legal sacrifice choice")
    void opponentCreatureIsNotChoosable() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        int lifeBefore = gd.getLife(player1.getId());

        castDisciple();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Counters contribute to the sacrificed creature's last battlefield power")
    void countersContributeToSacrificedPower() {
        var giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        giant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        castDisciple();

        harness.assertLife(player1, lifeBefore + 4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertOnBattlefield(player1, "Disciple of Bolas");
    }

    @Test
    @DisplayName("A negative-power creature is sacrificed without gaining life or drawing")
    void negativePowerGainsNothing() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        PermanentGrants.setPowerBoost(gd, bears, -3);
        int lifeBefore = gd.getLife(player1.getId());

        castDisciple();

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature that cannot be sacrificed is excluded from the mandatory choice")
    void cannotSacrificeProtectedCreature() {
        harness.addToBattlefield(player1, new AlexiosDeimosOfKosmos());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castDisciple();

        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertOnBattlefield(player1, "Alexios, Deimos of Kosmos");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("With only an unsacrificable other creature the trigger does nothing")
    void noSacrificableCreatureDoesNothing() {
        harness.addToBattlefield(player1, new AlexiosDeimosOfKosmos());
        int lifeBefore = gd.getLife(player1.getId());

        castDisciple();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Alexios, Deimos of Kosmos");
        harness.assertOnBattlefield(player1, "Disciple of Bolas");
    }

    @Test
    @DisplayName("The original trigger can sacrifice the Disciple after it is exiled and returned")
    void originalTriggerCanSacrificeReturnedDisciple() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new DiscipleOfBolas(), new Cloudshift()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        var originalId = harness.getPermanentId(player1, "Disciple of Bolas");
        harness.castAndResolveInstant(player1, 0, originalId);
        var returnedId = harness.getPermanentId(player1, "Disciple of Bolas");
        assertThat(returnedId).isNotEqualTo(originalId);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertNotOnBattlefield(player1, "Disciple of Bolas");
        harness.assertInGraveyard(player1, "Disciple of Bolas");
        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
