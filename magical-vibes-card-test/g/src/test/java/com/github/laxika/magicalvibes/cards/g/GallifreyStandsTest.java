package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.r.RoseTyler;
import com.github.laxika.magicalvibes.cards.t.TheEighthDoctor;
import com.github.laxika.magicalvibes.cards.t.TheEleventhDoctor;
import com.github.laxika.magicalvibes.cards.t.TheFifthDoctor;
import com.github.laxika.magicalvibes.cards.t.TheFirstDoctor;
import com.github.laxika.magicalvibes.cards.t.TheFourthDoctor;
import com.github.laxika.magicalvibes.cards.t.TheNinthDoctor;
import com.github.laxika.magicalvibes.cards.t.TheSecondDoctor;
import com.github.laxika.magicalvibes.cards.t.TheSeventhDoctor;
import com.github.laxika.magicalvibes.cards.t.TheSixthDoctor;
import com.github.laxika.magicalvibes.cards.t.TheTenthDoctor;
import com.github.laxika.magicalvibes.cards.t.TheThirdDoctor;
import com.github.laxika.magicalvibes.cards.t.TheThirteenthDoctor;
import com.github.laxika.magicalvibes.cards.t.TheTwelfthDoctor;
import com.github.laxika.magicalvibes.cards.t.TimeLordRegeneration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GallifreyStands.class, RoseTyler.class, TheFirstDoctor.class, TheSecondDoctor.class,
        TheThirdDoctor.class, TheFourthDoctor.class, TheFifthDoctor.class, TheSixthDoctor.class,
        TheSeventhDoctor.class, TheEighthDoctor.class, TheNinthDoctor.class, TheTenthDoctor.class,
        TheEleventhDoctor.class, TheTwelfthDoctor.class, TheThirteenthDoctor.class, TimeLordRegeneration.class})
class GallifreyStandsTest extends BaseCardTest {

    @Test
    void returnsAllDoctorsFromYourGraveyardToYourHand() {
        Card doctorOne = doctor("Doctor One");
        Card doctorTwo = doctor("Doctor Two");
        Card nonDoctor = new Card();
        nonDoctor.setName("Non-Doctor");
        nonDoctor.setType(CardType.INSTANT);
        harness.setGraveyard(player1, List.of(doctorOne, nonDoctor, doctorTwo));
        harness.setHand(player1, List.of(new GallifreyStands()));
        harness.addMana(player1, ManaColor.WHITE, 10);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(doctorOne, doctorTwo);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonDoctor);
    }

    @Test
    void mayPutADoctorCreatureFromHandOntoTheBattlefield() {
        Card doctor = doctor("Hand Doctor");
        harness.setHand(player1, List.of(doctor));
        harness.addToBattlefield(player1, new GallifreyStands());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(doctor);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == doctor);
    }

    @Test
    void winsAtTheBeginningOfUpkeepWithThirteenDoctors() {
        for (int i = 1; i <= 13; i++) {
            Card doctor = doctor("Doctor " + i);
            harness.addToBattlefield(player1, doctor);
        }
        harness.addToBattlefield(player1, new GallifreyStands());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @CardUsed(MaskwoodNexus.class)
    void returnsCreatureCardsThatAreDoctorsBecauseOfMaskwoodNexus() {
        Card rose = new RoseTyler();
        Card instant = new TimeLordRegeneration();
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.setGraveyard(player1, List.of(rose, instant));
        harness.setHand(player1, List.of(new GallifreyStands()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(rose);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
    }

    @Test
    void returnsOnlyItsControllersDoctors() {
        Card ownDoctor = new TheFirstDoctor();
        Card opposingDoctor = new TheSecondDoctor();
        Card nonDoctor = new RoseTyler();
        harness.setGraveyard(player1, List.of(ownDoctor, nonDoctor));
        harness.setGraveyard(player2, List.of(opposingDoctor));
        harness.setHand(player1, List.of(new GallifreyStands()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownDoctor);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonDoctor);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingDoctor);
    }

    @Test
    void puttingTheThirteenthDoctorOntoTheBattlefieldWinsImmediately() {
        addTwelveDoctors();
        Card lastDoctor = new TheSecondDoctor();
        harness.setHand(player1, List.of(lastDoctor));
        harness.addToBattlefield(player1, new GallifreyStands());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void decliningToPutTheThirteenthDoctorDoesNotWin() {
        addTwelveDoctors();
        Card lastDoctor = new TheSecondDoctor();
        harness.setHand(player1, List.of(lastDoctor));
        harness.addToBattlefield(player1, new GallifreyStands());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lastDoctor);
    }

    @Test
    void acceptingWithNoEligibleCardStillChecksTheWinCondition() {
        addTwelveDoctors();
        harness.addToBattlefield(player1, new TheSecondDoctor());
        Card nonDoctor = new RoseTyler();
        harness.setHand(player1, List.of(nonDoctor));
        harness.addToBattlefield(player1, new GallifreyStands());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonDoctor);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Card doctor = new TheSecondDoctor();
        harness.setHand(player1, List.of(doctor));
        harness.addToBattlefield(player1, new GallifreyStands());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(doctor);
        harness.assertNotOnBattlefield(player1, "The Second Doctor");
    }

    @Test
    void puttingADoctorIntoPlayDoesNotCountOpponentsDoctorsForTheWin() {
        Card doctor = new TheSecondDoctor();
        addTwelveDoctors(player2);
        harness.setHand(player1, List.of(new RoseTyler(), doctor, new TimeLordRegeneration()));
        harness.addToBattlefield(player1, new GallifreyStands());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);
        resolveAllTriggers();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertOnBattlefield(player1, "The Second Doctor");
        harness.assertInHand(player1, "Rose Tyler");
        harness.assertInHand(player1, "Time Lord Regeneration");
    }

    private void addTwelveDoctors() {
        addTwelveDoctors(player1);
    }

    private void addTwelveDoctors(Player player) {
        for (Card doctor : List.of(new TheFirstDoctor(), new TheThirdDoctor(), new TheFourthDoctor(),
                new TheFifthDoctor(), new TheSixthDoctor(), new TheSeventhDoctor(), new TheEighthDoctor(),
                new TheNinthDoctor(), new TheTenthDoctor(), new TheEleventhDoctor(), new TheTwelfthDoctor(),
                new TheThirteenthDoctor())) {
            harness.addToBattlefield(player, doctor);
        }
    }

    private Card doctor(String name) {
        Card doctor = new Card();
        doctor.setName(name);
        doctor.setType(CardType.CREATURE);
        doctor.setSubtypes(List.of(CardSubtype.DOCTOR));
        doctor.setPower(1);
        doctor.setToughness(1);
        return doctor;
    }
}
