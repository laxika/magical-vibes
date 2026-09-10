package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RecklessCohort;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrovetenderDruids.class, RecklessCohort.class, GrizzlyBears.class})
class GrovetenderDruidsTest extends BaseCardTest {

    @Test
    void ownAllyEntryMayCreatePlantToken() {
        harness.setHand(player1, List.of(new GrovetenderDruids()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertPlantToken();
    }

    @Test
    void anotherAllyEntryMayCreatePlantToken() {
        harness.addToBattlefield(player1, new GrovetenderDruids());
        harness.setHand(player1, List.of(new RecklessCohort()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertPlantToken();
    }

    @Test
    void nonAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new GrovetenderDruids());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Plant")).isEmpty();
    }

    @Test
    void decliningPaymentDoesNotCreatePlantToken() {
        harness.setHand(player1, List.of(new GrovetenderDruids()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Plant")).isEmpty();
    }

    private void assertPlantToken() {
        Permanent plant = findPermanent(player1, "Plant");
        assertThat(plant.getCard().isToken()).isTrue();
        assertThat(plant.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(plant.getCard().getPower()).isEqualTo(1);
        assertThat(plant.getCard().getToughness()).isEqualTo(1);
        assertThat(plant.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(plant.getCard().getSubtypes()).containsExactly(CardSubtype.PLANT);
    }
}
