package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HauntingVoyage.class, GrizzlyBears.class, AvianChangeling.class, HillGiant.class, SoulWarden.class})
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

        harness.castAndResolveSorcery(player1, 0, 0);
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
    @DisplayName("Normal casting may return zero creatures even when matching cards exist")
    void normalCastingMayReturnZeroCreatures() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));
        harness.setHand(player1, List.of(new HauntingVoyage()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleListChoice(player1, "BEAR");
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Normal casting may stop after returning one matching creature")
    void normalCastingMayReturnOnlyOneCreature() {
        GrizzlyBears bear = new GrizzlyBears();
        AvianChangeling changeling = new AvianChangeling();
        harness.setGraveyard(player1, List.of(bear, changeling));
        harness.setHand(player1, List.of(new HauntingVoyage()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleListChoice(player1, "BEAR");
        harness.handleGraveyardCardChosen(player1, 1);
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Avian Changeling")).hasSize(1);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear).doesNotContain(changeling);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature type with no matching cards may be chosen")
    void mayChooseCreatureTypeWithNoMatches() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));
        harness.setHand(player1, List.of(new HauntingVoyage()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleListChoice(player1, "GIANT");

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Foretold casting leaves matching creatures in the opponent's graveyard")
    void foretoldCastingOnlyReturnsControllersCreatures() {
        HauntingVoyage voyage = new HauntingVoyage();
        AvianChangeling changeling = new AvianChangeling();
        GrizzlyBears opposingBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(changeling));
        harness.setGraveyard(player2, List.of(opposingBear));
        harness.setHand(player1, List.of(voyage));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromExile(player1, voyage.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(findPermanents(player1, "Avian Changeling")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingBear);
        assertThat(gd.interaction.activeInteraction()).isNull();
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

    @Test
    @DisplayName("Foretold creatures enter simultaneously and see each other's entry")
    void foretoldCreaturesSeeEachOthersEntry() {
        HauntingVoyage voyage = new HauntingVoyage();
        harness.setGraveyard(player1, List.of(new AvianChangeling(), new SoulWarden()));
        harness.setHand(player1, List.of(voyage));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromExile(player1, voyage.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "HUMAN");

        harness.assertOnBattlefield(player1, "Avian Changeling");
        harness.assertOnBattlefield(player1, "Soul Warden");
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
