package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
