package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PageLooseLeaf.class, Forest.class, GrizzlyBears.class, Shock.class, RampantGrowth.class})
class PageLooseLeafTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Page adds {C}")
    void tapsForColorlessMana() {
        Permanent page = harness.addToBattlefieldAndReturn(player1, new PageLooseLeaf());
        page.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Grandeur discards another Page and puts the first instant or sorcery into hand")
    void grandeurFindsInstantOrSorcery() {
        PageLooseLeaf discardedPage = new PageLooseLeaf();
        Forest forest = new Forest();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new PageLooseLeaf());
        harness.setHand(player1, List.of(discardedPage, new GrizzlyBears()));
        harness.setLibrary(player1, List.of(forest, shock, bears));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedPage);
        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, bears);
    }

    @Test
    @DisplayName("Grandeur cannot be activated without another Page")
    void grandeurRequiresAnotherNamedCard() {
        harness.addToBattlefield(player1, new PageLooseLeaf());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grandeurFindsSorceryAndPreservesUnrevealedLibraryOrder() {
        PageLooseLeaf discardedPage = new PageLooseLeaf();
        Forest first = new Forest();
        GrizzlyBears second = new GrizzlyBears();
        RampantGrowth sorcery = new RampantGrowth();
        Shock unrevealedInstant = new Shock();
        Forest unrevealedLand = new Forest();
        harness.addToBattlefield(player1, new PageLooseLeaf());
        harness.setHand(player1, List.of(discardedPage));
        harness.setLibrary(player1, List.of(first, second, sorcery, unrevealedInstant, unrevealedLand));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedPage);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(unrevealedInstant, unrevealedLand);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 4))
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    void grandeurWithNoMatchReturnsAllCardsToLibrary() {
        PageLooseLeaf discardedPage = new PageLooseLeaf();
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new PageLooseLeaf());
        harness.setHand(player1, List.of(discardedPage));
        harness.setLibrary(player1, List.of(forest, bears));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedPage);
    }

    @Test
    void grandeurWithEmptyLibraryStillPaysDiscardCost() {
        PageLooseLeaf discardedPage = new PageLooseLeaf();
        harness.addToBattlefield(player1, new PageLooseLeaf());
        harness.setHand(player1, List.of(discardedPage));
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedPage);
    }

    @Test
    void grandeurWorksWhileTappedAndSummoningSick() {
        Permanent page = harness.addToBattlefieldAndReturn(player1, new PageLooseLeaf());
        page.setSummoningSick(true);
        page.tap();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(new PageLooseLeaf()));
        harness.setLibrary(player1, List.of(shock));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void summoningSicknessPreventsManaAbility() {
        Permanent page = harness.addToBattlefieldAndReturn(player1, new PageLooseLeaf());
        page.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
