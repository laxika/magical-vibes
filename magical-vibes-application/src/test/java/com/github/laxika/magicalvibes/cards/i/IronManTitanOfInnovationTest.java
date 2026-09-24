package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronManTitanOfInnovation.class, GoldenEgg.class, SolRing.class, WornPowerstone.class})
class IronManTitanOfInnovationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure, then sacrifices it to find an artifact with one higher mana value")
    void createsTreasureThenSearchesForArtifact() {
        addCreatureReady(player1, new IronManTitanOfInnovation());
        SolRing found = new SolRing();
        GoldenEgg wrongManaValue = new GoldenEgg();
        harness.setLibrary(player1, List.of(found, wrongManaValue));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent treasure = findPermanent(player1, "Treasure");
        harness.handlePermanentChosen(player1, treasure.getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(found);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        Permanent ring = findPermanent(player1, "Sol Ring");
        assertThat(ring.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wrongManaValue);
    }

    @Test
    @DisplayName("Uses the sacrificed noncreature artifact's mana value")
    void usesSacrificedArtifactManaValue() {
        addCreatureReady(player1, new IronManTitanOfInnovation());
        Permanent sacrificed = new Permanent(new GoldenEgg());
        gd.playerBattlefields.get(player1.getId()).add(sacrificed);

        WornPowerstone found = new WornPowerstone();
        SolRing wrongManaValue = new SolRing();
        harness.setLibrary(player1, List.of(found, wrongManaValue));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(found);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
        assertThat(findPermanent(player1, "Worn Powerstone").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wrongManaValue);
    }

    @Test
    @DisplayName("Declining the sacrifice leaves the created Treasure on the battlefield")
    void decliningSacrificeDoesNothing() {
        addCreatureReady(player1, new IronManTitanOfInnovation());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
