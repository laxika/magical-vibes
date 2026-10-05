package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProsperousPartnership.class, GrizzlyBears.class})
class ProsperousPartnershipTest extends BaseCardTest {

    @Test
    @DisplayName("When Prosperous Partnership enters, it creates two Citizen tokens")
    void enteringCreatesTwoCitizenTokens() {
        harness.setHand(player1, List.of(new ProsperousPartnership()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        List<Permanent> citizens = findPermanents(player1, "Citizen");
        assertThat(citizens).hasSize(2);
        assertThat(citizens).allSatisfy(citizen -> {
            assertThat(citizen.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(citizen.getCard().getPower()).isEqualTo(1);
            assertThat(citizen.getCard().getToughness()).isEqualTo(1);
            assertThat(citizen.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(citizen.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
            assertThat(citizen.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
        });
    }

    @Test
    @DisplayName("Tapping three untapped creatures creates a Treasure token")
    void tappingThreeCreaturesCreatesTreasure() {
        harness.addToBattlefield(player1, new ProsperousPartnership());
        Permanent creatureA = addCreatureReady(player1, new GrizzlyBears());
        Permanent creatureB = addCreatureReady(player1, new GrizzlyBears());
        Permanent creatureC = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(creatureA.isTapped()).isTrue();
        assertThat(creatureB.isTapped()).isTrue();
        assertThat(creatureC.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("The Treasure ability requires three untapped creatures")
    void requiresThreeUntappedCreatures() {
        harness.addToBattlefield(player1, new ProsperousPartnership());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Newly created Citizens can pay the cost and only the chosen three tap")
    void summoningSickCitizensCanPayTheCost() {
        harness.enterBattlefieldAndReturn(player1, new ProsperousPartnership());
        harness.enterBattlefieldAndReturn(player1, new ProsperousPartnership());
        resolveAllTriggers();
        List<Permanent> citizens = findPermanents(player1, "Citizen");
        assertThat(citizens).hasSize(4);
        assertThat(citizens).allSatisfy(citizen -> assertThat(citizen.isSummoningSick()).isTrue());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, citizens.get(0).getId());
        harness.handlePermanentChosen(player1, citizens.get(1).getId());
        harness.handlePermanentChosen(player1, citizens.get(2).getId());

        assertThat(citizens.subList(0, 3)).allSatisfy(citizen -> assertThat(citizen.isTapped()).isTrue());
        assertThat(citizens.get(3).isTapped()).isFalse();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("An already tapped creature cannot pay the cost")
    void tappedCreatureCannotPayTheCost() {
        harness.addToBattlefield(player1, new ProsperousPartnership());
        Permanent creatureA = addCreatureReady(player1, new GrizzlyBears());
        Permanent creatureB = addCreatureReady(player1, new GrizzlyBears());
        Permanent creatureC = addCreatureReady(player1, new GrizzlyBears());
        creatureC.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creatureA.isTapped()).isFalse();
        assertThat(creatureB.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the cost")
    void opponentsCreatureCannotPayTheCost() {
        harness.addToBattlefield(player1, new ProsperousPartnership());
        Permanent creatureA = addCreatureReady(player1, new GrizzlyBears());
        Permanent creatureB = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentsCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creatureA.isTapped()).isFalse();
        assertThat(creatureB.isTapped()).isFalse();
        assertThat(opponentsCreature.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
}
