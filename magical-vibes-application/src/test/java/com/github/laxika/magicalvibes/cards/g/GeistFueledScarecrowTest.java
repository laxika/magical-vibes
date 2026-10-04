package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BloodPet;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeistFueledScarecrow.class, BloodPet.class, DarkRitual.class})
class GeistFueledScarecrowTest extends BaseCardTest {

    @Test
    void controllerCreatureSpellCostsOneMore() {
        harness.addToBattlefield(player1, new GeistFueledScarecrow());
        harness.setHand(player1, List.of(new BloodPet()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentCreatureSpellIsNotTaxed() {
        harness.addToBattlefield(player1, new GeistFueledScarecrow());

        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BloodPet()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void controllerNoncreatureSpellIsNotTaxed() {
        harness.addToBattlefield(player1, new GeistFueledScarecrow());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void artifactCreatureSpellIsTaxed() {
        harness.addToBattlefield(player1, new GeistFueledScarecrow());
        harness.setHand(player1, List.of(new GeistFueledScarecrow()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void multipleScarecrowsApplyCumulativeTaxes() {
        harness.addToBattlefield(player1, new GeistFueledScarecrow());
        harness.addToBattlefield(player1, new GeistFueledScarecrow());
        harness.setHand(player1, List.of(new GeistFueledScarecrow()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void scarecrowsOutsideBattlefieldDoNotIncreaseCastingCost() {
        harness.setGraveyard(player1, List.of(new GeistFueledScarecrow()));
        harness.setHand(player1, List.of(new GeistFueledScarecrow(), new GeistFueledScarecrow()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
