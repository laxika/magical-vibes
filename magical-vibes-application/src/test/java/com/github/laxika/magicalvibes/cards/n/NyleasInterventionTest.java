package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NyleasIntervention.class, AirElemental.class, Forest.class, GrizzlyBears.class,
        Island.class, SuntailHawk.class})
class NyleasInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Search mode finds up to X land cards and excludes nonlands")
    void searchModeFindsUpToXLands() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, bears, island));
        harness.setHand(player1, List.of(new NyleasIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 2, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest, island);
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().canFailToFind()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest)
                .doesNotContain(bears, island);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Damage mode deals twice X damage only to creatures with flying")
    void damageModeDealsTwiceXToFlyers() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NyleasIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 2, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
