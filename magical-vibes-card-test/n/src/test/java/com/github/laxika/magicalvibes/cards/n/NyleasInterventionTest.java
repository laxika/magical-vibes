package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TempleOfPlenty;
import com.github.laxika.magicalvibes.cards.v.VexingGull;
import com.github.laxika.magicalvibes.cards.w.WitnessOfTomorrows;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NyleasIntervention.class, WitnessOfTomorrows.class, Forest.class, NyxbornCourser.class,
        Island.class, VexingGull.class, TempleOfPlenty.class})
class NyleasInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Search mode finds up to X land cards and excludes nonlands")
    void searchModeFindsUpToXLands() {
        Forest forest = new Forest();
        NyxbornCourser courser = new NyxbornCourser();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, courser, island));
        harness.setHand(player1, List.of(new NyleasIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 2, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest, island);
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().canFailToFind()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest)
                .doesNotContain(courser, island);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Damage mode deals twice X damage only to creatures with flying")
    void damageModeDealsTwiceXToFlyers() {
        harness.addToBattlefield(player1, new VexingGull());
        harness.addToBattlefield(player2, new WitnessOfTomorrows());
        harness.addToBattlefield(player2, new NyxbornCourser());
        harness.setHand(player1, List.of(new NyleasIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 2, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vexing Gull");
        harness.assertNotOnBattlefield(player2, "Witness of Tomorrows");
        harness.assertOnBattlefield(player2, "Nyxborn Courser");
    }

    @Test
    void searchFindsNonbasicLandsAndStopsAtX() {
        TempleOfPlenty temple = new TempleOfPlenty();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(temple, forest, island));
        harness.setHand(player1, List.of(new NyleasIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 2, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(temple, forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void searchCanFindNoLandsEvenWhenAvailable() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new NyleasIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 1, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void zeroXSearchFinishesWithoutTakingCards() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new NyleasIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void damageMarksTwiceXWithoutDamagingGroundCreaturesOrPlayers() {
        harness.addToBattlefield(player2, new WitnessOfTomorrows());
        harness.addToBattlefield(player2, new NyxbornCourser());
        harness.setHand(player1, List.of(new NyleasIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Witness of Tomorrows").getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Nyxborn Courser").getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void zeroXDamageLeavesFlyingCreaturesUndamaged() {
        harness.addToBattlefield(player1, new VexingGull());
        harness.addToBattlefield(player2, new WitnessOfTomorrows());
        harness.setHand(player1, List.of(new NyleasIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 0, List.of());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Vexing Gull").getMarkedDamage()).isZero();
        assertThat(findPermanent(player2, "Witness of Tomorrows").getMarkedDamage()).isZero();
    }
}
