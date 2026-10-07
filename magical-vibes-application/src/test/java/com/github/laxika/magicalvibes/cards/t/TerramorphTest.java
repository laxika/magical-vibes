package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Terramorph.class, Forest.class, Island.class, GrizzlyBears.class, TerramorphicExpanse.class})
class TerramorphTest extends BaseCardTest {

    @Test
    void putsABasicLandOntoTheBattlefieldAndExcludesNonlands() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));
        harness.setHand(player1, List.of(new Terramorph()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void reboundSearchesAgainAtTheNextUpkeep() {
        Forest forest = new Forest();
        Island island = new Island();
        Terramorph card = new Terramorph();
        harness.setLibrary(player1, List.of(forest, island));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertInGraveyard(player1, "Terramorph");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    @CardUsed(TerramorphicExpanse.class)
    void excludesNonbasicLandsAndPutsTheChosenBasicLandOntoTheBattlefieldUntapped() {
        Forest forest = new Forest();
        TerramorphicExpanse nonbasic = new TerramorphicExpanse();
        harness.setLibrary(player1, List.of(nonbasic, forest));
        harness.setHand(player1, List.of(new Terramorph()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(forest.getId());
                    assertThat(permanent.isTapped()).isFalse();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasic);
    }

    @Test
    void canFailToFindAndDeclineReboundWithoutGettingAnotherOpportunity() {
        Forest forest = new Forest();
        Terramorph card = new Terramorph();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Terramorph");
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void resolvesAndReboundsEvenWhenTheLibraryIsEmpty() {
        Terramorph card = new Terramorph();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Terramorph");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
