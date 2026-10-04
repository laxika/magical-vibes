package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FarrelitePriest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HomaridSpawningBed.class, Homarid.class, HomaridWarrior.class, FarrelitePriest.class})
class HomaridSpawningBedTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a blue creature creates Camarids equal to its mana value")
    void createsCamaridsEqualToSacrificedCreatureManaValue() {
        harness.addToBattlefield(player1, new HomaridSpawningBed());
        Permanent homarid = addCreatureReady(player1, new Homarid());
        addCreatureReady(player1, new HomaridWarrior());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, homarid.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Homarid");
        assertThat(findPermanents(player1, "Camarid"))
                .hasSize(3)
                .allSatisfy(camarid -> {
                    assertThat(camarid.getCard().getColor()).isEqualTo(CardColor.BLUE);
                    assertThat(camarid.getCard().getSubtypes()).contains(CardSubtype.CAMARID);
                    assertThat(camarid.getCard().getPower()).isEqualTo(1);
                    assertThat(camarid.getCard().getToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Only blue creatures can be sacrificed")
    void onlyBlueCreaturesCanBeSacrificed() {
        harness.addToBattlefield(player1, new HomaridSpawningBed());
        addCreatureReady(player1, new Homarid());
        addCreatureReady(player1, new HomaridWarrior());
        Permanent priest = addCreatureReady(player1, new FarrelitePriest());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, priest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a blue creature to sacrifice")
    void cannotActivateWithoutBlueCreature() {
        harness.addToBattlefield(player1, new HomaridSpawningBed());
        addCreatureReady(player1, new FarrelitePriest());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates five Camarids for a five-mana-value sacrificed creature")
    void createsCamaridsEqualToFiveManaValue() {
        harness.addToBattlefield(player1, new HomaridSpawningBed());
        addCreatureReady(player1, new HomaridWarrior());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Homarid Warrior");
        assertThat(countPermanents(player1, "Camarid")).isEqualTo(5);
    }

    @Test
    @DisplayName("A Camarid token can be sacrificed but creates no new tokens")
    void sacrificingCamaridCreatesNoTokens() {
        harness.addToBattlefield(player1, new HomaridSpawningBed());
        addCreatureReady(player1, new HomaridWarrior());
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Homarid Warrior");
        assertThat(countPermanents(player1, "Camarid")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Camarid")).isEqualTo(5);

        Permanent camarid = findPermanent(player1, "Camarid");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, camarid.getId());
        assertThat(countPermanents(player1, "Camarid")).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Camarid")).isEqualTo(4);
        assertThat(countPermanents(player2, "Camarid")).isZero();
    }

    @Test
    @DisplayName("An opponent's blue creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new HomaridSpawningBed());
        addCreatureReady(player2, new HomaridWarrior());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Homarid Warrior");
    }

    @Test
    @DisplayName("A tapped blue creature with shroud can be sacrificed")
    void canSacrificeTappedCreatureWithShroud() {
        harness.addToBattlefield(player1, new HomaridSpawningBed());
        addCreatureReady(player1, new HomaridWarrior());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Homarid Warrior").isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Homarid Warrior");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Camarid")).isEqualTo(5);
    }
}
