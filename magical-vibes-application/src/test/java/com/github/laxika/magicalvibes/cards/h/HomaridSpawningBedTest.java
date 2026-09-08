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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Camarid"))
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
}
