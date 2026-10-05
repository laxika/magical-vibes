package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LootExuberantExplorer.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class, Shock.class})
class LootExuberantExplorerTest extends BaseCardTest {

    @Test
    @DisplayName("Controller may play one additional land each turn; opponents may not")
    void grantsControllerOneExtraLandPlay() {
        harness.addToBattlefield(player1, new LootExuberantExplorer());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability offers only creatures within the number of lands controlled")
    void abilityUsesControlledLandCountAsManaValueCap() {
        addCreatureReady(player1, new LootExuberantExplorer());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(
                bears,
                new HillGiant(),
                elves,
                new Shock(),
                new Forest(),
                new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId(), elves.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    void mayDeclineEligibleCreatureAndBottomOnlyTopSix() {
        addCreatureReady(player1, new LootExuberantExplorer());
        harness.addToBattlefield(player1, new Forest());
        LlanowarElves elves = new LlanowarElves();
        List<Card> lookedAt = List.of(
                elves, new Forest(), new Shock(), new Forest(), new Shock(), new Forest());
        Forest untouched = new Forest();
        ArrayList<Card> library =
                new ArrayList<>(lookedAt);
        library.add(untouched);
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noEligibleCreatureInShortLibraryFinishesWithoutChoice() {
        addCreatureReady(player1, new LootExuberantExplorer());
        harness.addToBattlefield(player2, new Forest());
        LlanowarElves elves = new LlanowarElves();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(elves, forest));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(elves, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void evaluatesLandCountAtResolutionAndPutsCreatureUntapped() {
        addCreatureReady(player1, new LootExuberantExplorer());
        harness.addToBattlefield(player1, new Forest());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(findPermanent(player1, "Loot, Exuberant Explorer").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Grizzly Bears").isSummoningSick()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
