package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PolymorphistsJest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObNixilisUnshackled.class, DiabolicTutor.class, GiantSpider.class,
        RuneclawBear.class, Plains.class, Shock.class, Swamp.class, PolymorphistsJest.class,
        WitchbaneOrb.class})
class ObNixilisUnshackledTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent searching their library sacrifices a creature and loses 10 life")
    void opponentSearchSacrificesAndDrainsTen() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setLife(player1, 20);

        castTutorAndFinishSearch();

        harness.passBothPriorities(); // resolve Ob Nixilis' search trigger

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);

        // The sacrificed creature dying also triggers Ob Nixilis' own +1/+1 counter ability.
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Ob Nixilis, Unshackled")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The searching player chooses which creature to sacrifice")
    void searchingPlayerChoosesSacrifice() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new GiantSpider());

        castTutorAndFinishSearch();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertOnBattlefield(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Life loss still happens when the searching player controls no creature")
    void losesLifeWithoutCreature() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.setLife(player1, 20);

        castTutorAndFinishSearch();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller's own library search does not trigger it")
    void ownSearchDoesNotTrigger() {
        harness.addToBattlefield(player1, new ObNixilisUnshackled());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setLife(player1, 20);

        castTutorAndFinishSearch();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when another creature dies")
    void getsCounterWhenAnotherCreatureDies() {
        harness.addToBattlefield(player1, new ObNixilisUnshackled());
        harness.addToBattlefield(player2, new RuneclawBear());

        Permanent obNixilis = findPermanent(player1, "Ob Nixilis, Unshackled");
        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Ob Nixilis' counter trigger resolves

        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, obNixilis)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, obNixilis)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not get a counter when Ob Nixilis itself dies")
    void noCounterWhenItselfDies() {
        harness.addToBattlefield(player1, new ObNixilisUnshackled());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        UUID obId = harness.getPermanentId(player1, "Ob Nixilis, Unshackled");
        harness.castAndResolveInstant(player2, 0, obId);
        harness.castAndResolveInstant(player2, 0, obId);

        harness.assertNotOnBattlefield(player1, "Ob Nixilis, Unshackled");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The library-search ability affects an opponent with hexproof")
    void searchTriggerDoesNotTargetSearchingPlayer() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.addToBattlefield(player1, new WitchbaneOrb());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setLife(player1, 20);

        castTutorAndFinishSearch();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Ob Nixilis, Unshackled")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Library searches do not trigger Ob Nixilis while it has lost all abilities")
    void noSearchTriggerWhileAbilitiesAreRemoved() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PolymorphistsJest()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        castTutorAndFinishSearch();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Creature deaths do not trigger Ob Nixilis while it has lost all abilities")
    void noDeathTriggerWhileAbilitiesAreRemoved() {
        Permanent obNixilis = harness.addToBattlefieldAndReturn(player2, new ObNixilisUnshackled());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new PolymorphistsJest(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.stack).isEmpty();
        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Searching an empty library still causes the sacrifice and life loss")
    void emptyLibrarySearchStillTriggers() {
        Permanent obNixilis = harness.addToBattlefieldAndReturn(player2, new ObNixilisUnshackled());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A library-search trigger still resolves after Ob Nixilis dies")
    void searchTriggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setLife(player1, 20);

        castTutorAndFinishSearch();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        UUID obId = harness.getPermanentId(player2, "Ob Nixilis, Unshackled");
        harness.castAndResolveInstant(player1, 0, obId);
        harness.castAndResolveInstant(player1, 0, obId);
        harness.assertNotOnBattlefield(player2, "Ob Nixilis, Unshackled");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gets a counter when another creature with the same controller dies")
    void friendlyCreatureDeathAlsoTriggers() {
        Permanent obNixilis = harness.addToBattlefieldAndReturn(player1, new ObNixilisUnshackled());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    /** Player 1 casts Diabolic Tutor and completes the library search it starts. */
    private void castTutorAndFinishSearch() {
        harness.setLibrary(player1, List.of(new Plains(), new Swamp()));

        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
    }
}
