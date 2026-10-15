package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronManTitanOfInnovation.class, SolRing.class, MindStone.class, DarksteelIngot.class, IronMyr.class})
class IronManTitanOfInnovationTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a Treasure and can sacrifice an artifact to find one with one higher mana value")
    void attacksCreatesTreasureAndFindsArtifact() {
        addCreatureReady(player1, new IronManTitanOfInnovation());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        DarksteelIngot foundArtifact = new DarksteelIngot();
        harness.setLibrary(player1, List.of(foundArtifact));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, mindStone.getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(foundArtifact);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Darksteel Ingot").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Mind Stone");
    }

    @Test
    @DisplayName("Declining the sacrifice still leaves the created Treasure")
    void decliningSacrificeStillCreatesTreasure() {
        addCreatureReady(player1, new IronManTitanOfInnovation());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An artifact creature cannot be sacrificed for the ability")
    void cannotSacrificeArtifactCreature() {
        addCreatureReady(player1, new IronManTitanOfInnovation());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent ironMyr = harness.addToBattlefieldAndReturn(player1, new IronMyr());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent treasure = findPermanent(player1, "Treasure");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(mindStone.getId(), treasure.getId())
                .doesNotContain(ironMyr.getId());
        harness.handlePermanentChosen(player1, mindStone.getId());

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ironMyr);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The newly created Treasure can be sacrificed to find a one-mana artifact")
    void canSacrificeCreatedTreasure() {
        addCreatureReady(player1, new IronManTitanOfInnovation());
        SolRing solRing = new SolRing();
        harness.setLibrary(player1, List.of(solRing, new DarksteelIngot()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Treasure").getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(solRing);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanent(player1, "Sol Ring").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The search requires exactly one higher mana value and permits failing to find")
    void exactManaValueSearchCanFailToFind() {
        addCreatureReady(player1, new IronManTitanOfInnovation());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        SolRing lower = new SolRing();
        DarksteelIngot matching = new DarksteelIngot();
        IronManTitanOfInnovation higher = new IronManTitanOfInnovation();
        harness.setLibrary(player1, List.of(lower, matching, higher));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, mindStone.getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matching);
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Darksteel Ingot");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(lower, matching, higher);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Artifact creatures can be found, but opponents' artifacts cannot be sacrificed")
    void canFindArtifactCreatureAndCannotSacrificeOpponentsArtifact() {
        addCreatureReady(player1, new IronManTitanOfInnovation());
        Permanent solRing = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new SolRing());
        IronMyr ironMyr = new IronMyr();
        harness.setLibrary(player1, List.of(ironMyr));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(solRing.getId(), findPermanent(player1, "Treasure").getId())
                .doesNotContain(opposingArtifact.getId());
        harness.handlePermanentChosen(player1, solRing.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Iron Myr").isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingArtifact);
        harness.assertInGraveyard(player1, "Sol Ring");
    }
}
