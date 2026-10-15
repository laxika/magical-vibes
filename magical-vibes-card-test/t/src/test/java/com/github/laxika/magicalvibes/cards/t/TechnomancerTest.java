package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Technomancer.class, Forest.class, GrizzlyBears.class, MyrRetriever.class,
        WurmcoilEngine.class, MindStone.class})
class TechnomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by milling three cards, then returns artifact creatures within total mana value six")
    void millsThenReturnsArtifactCreaturesWithinManaValueCap() {
        MyrRetriever firstMyr = new MyrRetriever();
        MyrRetriever secondMyr = new MyrRetriever();
        WurmcoilEngine wurmcoil = new WurmcoilEngine();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstMyr, secondMyr, wurmcoil, bears));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castTechnomancer();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstMyr, secondMyr, wurmcoil, bears);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds())
                .containsExactlyInAnyOrder(firstMyr.getId(), secondMyr.getId(), wurmcoil.getId())
                .doesNotContain(bears.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstMyr.getId(), secondMyr.getId(), wurmcoil.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value");

        harness.handleMultipleCardsChosen(player1, List.of(firstMyr.getId(), secondMyr.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(firstMyr, secondMyr)
                .doesNotContain(wurmcoil, bears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(wurmcoil.getId(), bears.getId());
    }

    @Test
    @DisplayName("The controller may return no artifact creatures")
    void mayReturnNoArtifactCreatures() {
        MyrRetriever myr = new MyrRetriever();
        harness.setGraveyard(player1, List.of(myr));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castTechnomancer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .doesNotContain(myr);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(myr.getId());
    }

    @Test
    @DisplayName("A newly milled artifact creature with mana value exactly six can return")
    void returnsNewlyMilledCreatureAtManaValueLimit() {
        WurmcoilEngine wurmcoil = new WurmcoilEngine();
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        Forest remainingForest = new Forest();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(wurmcoil, firstForest, secondForest, remainingForest));

        castTechnomancer();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingForest);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(wurmcoil.getId());
        harness.handleMultipleCardsChosen(player1, List.of(wurmcoil.getId()));

        harness.assertOnBattlefield(player1, "Wurmcoil Engine");
        harness.assertNotInGraveyard(player1, "Wurmcoil Engine");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstForest, secondForest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == wurmcoil)
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isFalse());
    }

    @Test
    @DisplayName("Milling fewer than three cards still permits returning artifact creatures")
    void shortLibraryDoesNotPreventReturn() {
        MyrRetriever myr = new MyrRetriever();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(myr));

        castTechnomancer();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.handleMultipleCardsChosen(player1, List.of(myr.getId()));
        harness.assertOnBattlefield(player1, "Myr Retriever");
        harness.assertNotInGraveyard(player1, "Myr Retriever");
    }

    @Test
    @DisplayName("Three artifact creatures with total mana value six can all return")
    void returnsMultipleCreaturesAtCombinedManaValueLimit() {
        MyrRetriever first = new MyrRetriever();
        MyrRetriever second = new MyrRetriever();
        MyrRetriever third = new MyrRetriever();
        MyrRetriever opponentMyr = new MyrRetriever();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setGraveyard(player2, List.of(opponentMyr));
        harness.setLibrary(player1, List.of());

        castTechnomancer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .contains(first, second, third).doesNotContain(opponentMyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentMyr);
    }

    @Test
    @DisplayName("The trigger finishes when no artifact creature has mana value six or less")
    void noEligibleCardsFinishesWithoutChoice() {
        Technomancer expensiveCreature = new Technomancer();
        GrizzlyBears nonartifact = new GrizzlyBears();
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(expensiveCreature, nonartifact));
        harness.setLibrary(player1, List.of(land));

        castTechnomancer();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(expensiveCreature, nonartifact, land);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .doesNotContain(expensiveCreature, nonartifact, land);
    }

    @Test
    @DisplayName("Noncreature artifacts cannot be returned")
    void noncreatureArtifactsAreNotEligible() {
        MindStone artifact = new MindStone();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLibrary(player1, List.of());

        castTechnomancer();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Mind Stone");
    }

    private void castTechnomancer() {
        harness.castFromHand(player1, new Technomancer(), "{5}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
