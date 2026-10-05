package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.cards.t.TempleGarden;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NervousGardener.class, Forest.class, TempleGarden.class, RakdosGuildgate.class})
class NervousGardenerTest extends BaseCardTest {

    @Test
    void turningFaceUpSearchesForALandWithABasicLandType() {
        Forest forest = new Forest();
        TempleGarden templeGarden = new TempleGarden();
        RakdosGuildgate guildgate = new RakdosGuildgate();
        harness.setLibrary(player1, List.of(forest, templeGarden, guildgate));
        harness.setHand(player1, List.of(new NervousGardener()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent gardener = findPermanent(player1, "Nervous Gardener");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gardener));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest, templeGarden);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Temple Garden");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(guildgate, forest);
    }

    @Test
    void canFailToFindEvenWhenAnEligibleLandExists() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        castFaceDownAndTurnFaceUp();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void searchCompletesWhenLibraryContainsNoEligibleLand() {
        NervousGardener otherGardener = new NervousGardener();
        harness.setLibrary(player1, List.of(otherGardener));
        castFaceDownAndTurnFaceUp();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherGardener);
    }

    @Test
    void castingFaceUpDoesNotSearch() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new NervousGardener()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nervous Gardener");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    private void castFaceDownAndTurnFaceUp() {
        harness.setHand(player1, List.of(new NervousGardener()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent gardener = findPermanent(player1, "Nervous Gardener");
        assertThat(gardener.isFaceDown()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gardener));
        assertThat(gardener.isFaceDown()).isFalse();
        harness.passBothPriorities();
    }
}
