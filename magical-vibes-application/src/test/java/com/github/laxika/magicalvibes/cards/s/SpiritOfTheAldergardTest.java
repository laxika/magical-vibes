package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AxgardCavalry;
import com.github.laxika.magicalvibes.cards.r.RimewoodFalls;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritOfTheAldergard.class, SnowCoveredForest.class, SnowCoveredIsland.class,
        Forest.class, AxgardCavalry.class, SculptorOfWinter.class, RimewoodFalls.class})
class SpiritOfTheAldergardTest extends BaseCardTest {

    @Test
    @DisplayName("Counts other snow permanents you control")
    void countsOtherSnowPermanentsYouControl() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SpiritOfTheAldergard());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredIsland());

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count an opponent's or nonsnow permanent")
    void doesNotCountOpponentOrNonsnowPermanent() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SpiritOfTheAldergard());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new SnowCoveredForest());

        assertThat(gqs.getEffectivePower(gd, spirit)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enters and searches for a snow land")
    void entersAndSearchesForSnowLand() {
        harness.setHand(player1, List.of(new SpiritOfTheAldergard()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLibrary(player1, List.of(new SnowCoveredForest(), new Forest(), new AxgardCavalry()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Snow-Covered Forest");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Snow-Covered Forest");
    }

    @Test
    @DisplayName("Counts snow creatures and other copies independently")
    void countsSnowCreaturesAndOtherCopies() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SpiritOfTheAldergard());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SpiritOfTheAldergard());
        harness.addToBattlefield(player1, new SculptorOfWinter());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power updates when other snow permanents leave")
    void powerUpdatesWhenSnowPermanentLeaves() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SpiritOfTheAldergard());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerGraveyards.get(player1.getId()).add(land.getCard());

        assertThat(gqs.getEffectivePower(gd, spirit)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can find a nonbasic snow land but not a snow creature")
    void findsNonbasicSnowLandButNotSnowCreature() {
        RimewoodFalls land = new RimewoodFalls();
        SculptorOfWinter creature = new SculptorOfWinter();
        harness.setLibrary(player1, List.of(creature, land, new Forest()));
        castAndResolveEnterTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(land);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).contains(creature).doesNotContain(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals Rimewood Falls")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Can fail to find even when a snow land is available")
    void canFailToFindAvailableSnowLand() {
        SnowCoveredForest land = new SnowCoveredForest();
        harness.setLibrary(player1, List.of(land));
        castAndResolveEnterTrigger();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Search with no snow lands completes and shuffles")
    void noSnowLandsCompletesAndShuffles() {
        Forest land = new Forest();
        SculptorOfWinter creature = new SculptorOfWinter();
        harness.setLibrary(player1, List.of(land, creature));
        castAndResolveEnterTrigger();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Search with an empty library completes")
    void emptyLibraryCompletes() {
        harness.setLibrary(player1, List.of());
        castAndResolveEnterTrigger();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolveEnterTrigger() {
        harness.setHand(player1, List.of(new SpiritOfTheAldergard()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
