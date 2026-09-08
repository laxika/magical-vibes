package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HauntingVoyageTest extends BaseCardTest {

    @Test
    @DisplayName("Normal casting returns up to two creatures of the chosen type")
    void normalCastingReturnsUpToTwoChosenTypeCreatures() {
        GrizzlyBears firstBear = new GrizzlyBears();
        GrizzlyBears secondBear = new GrizzlyBears();
        AvianChangeling changeling = new AvianChangeling();
        HillGiant giant = new HillGiant();
        harness.setGraveyard(player1, List.of(firstBear, secondBear, changeling, giant));
        harness.setHand(player1, List.of(new HauntingVoyage()));
        addNormalMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player1, "Avian Changeling")).isEmpty();
        assertThat(findPermanents(player1, "Hill Giant")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(changeling, giant);
    }

    @Test
    @DisplayName("Foretold casting returns all creatures of the chosen type")
    void foretoldCastingReturnsAllChosenTypeCreatures() {
        HauntingVoyage voyage = new HauntingVoyage();
        GrizzlyBears firstBear = new GrizzlyBears();
        GrizzlyBears secondBear = new GrizzlyBears();
        AvianChangeling changeling = new AvianChangeling();
        HillGiant giant = new HillGiant();
        harness.setGraveyard(player1, List.of(firstBear, secondBear, changeling, giant));
        harness.setHand(player1, List.of(voyage));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(voyage.getId());
        assertThat(entry).isNotNull();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromExile(player1, voyage.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player1, "Avian Changeling")).hasSize(1);
        assertThat(findPermanents(player1, "Hill Giant")).isEmpty();
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
