package com.github.laxika.magicalvibes.cards.k;

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

@CardUsed({KhalniGarden.class})
class KhalniGardenTest extends BaseCardTest {

    @Test
    @DisplayName("Khalni Garden enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new KhalniGarden()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Khalni Garden").isTapped()).isTrue();
    }

    @Test
    @DisplayName("When Khalni Garden enters, it creates a 0/1 green Plant token")
    void createsPlantToken() {
        harness.setHand(player1, List.of(new KhalniGarden()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent plant = findPermanent(player1, "Plant");
        assertThat(plant.getCard().isToken()).isTrue();
        assertThat(plant.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(plant.getCard().getPower()).isEqualTo(0);
        assertThat(plant.getCard().getToughness()).isEqualTo(1);
        assertThat(plant.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(plant.getCard().getSubtypes()).containsExactly(CardSubtype.PLANT);
    }

    @Test
    @DisplayName("Tapping Khalni Garden adds one green mana")
    void tapsForGreenMana() {
        Permanent garden = harness.addToBattlefieldAndReturn(player1, new KhalniGarden());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(garden.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Plant is created only when the enters trigger resolves, and enters untapped")
    void plantCreationUsesTheStack() {
        harness.setHand(player1, List.of(new KhalniGarden()));

        harness.playLand(player1, 0);

        assertThat(countPermanents(player1, "Khalni Garden")).isEqualTo(1);
        assertThat(countPermanents(player1, "Plant")).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Plant")).isEqualTo(1);
        assertThat(findPermanent(player1, "Plant").isTapped()).isFalse();
        assertThat(countPermanents(player2, "Plant")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being played still creates a Plant for the land's controller")
    void enteringUnderOpponentsControlCreatesTheirPlant() {
        Permanent garden = harness.enterBattlefieldAndReturn(player2, new KhalniGarden());

        assertThat(garden.isTapped()).isTrue();
        assertThat(countPermanents(player2, "Plant")).isZero();

        resolveAllTriggers();

        assertThat(countPermanents(player2, "Plant")).isEqualTo(1);
        assertThat(findPermanent(player2, "Plant").isTapped()).isFalse();
        assertThat(countPermanents(player1, "Plant")).isZero();
    }
}
