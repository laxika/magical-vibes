package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GallifreyStands.class)
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
        harness.passBothPriorities();
        harness.passBothPriorities();

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
