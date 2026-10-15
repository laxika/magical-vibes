package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BakersbaneDuo;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeapedHarvest.class, Forest.class, Island.class, Plains.class, BakersbaneDuo.class})
class HeapedHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield offers a basic land search")
    void enteringOffersBasicLandSearch() {
        setupAndCast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the enter trigger puts a basic land onto the battlefield tapped")
    void acceptingEnterTriggerPutsBasicLandTapped() {
        setupAndCast();
        setupLibrary();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .hasSize(3)
                .allMatch(card -> card.hasType(CardType.LAND));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
    }

    @Test
    @DisplayName("Sacrificing gains 3 life and offers the same land search")
    void sacrificingGainsLifeAndOffersSearch() {
        Permanent harvest = harness.addToBattlefieldAndReturn(player1, new HeapedHarvest());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibrary();
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Heaped Harvest");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
        assertThat(harvest).isNotIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("Removal other than sacrifice does not trigger the search")
    void nonsacrificeRemovalDoesNotTriggerSearch() {
        Permanent harvest = harness.addToBattlefieldAndReturn(player1, new HeapedHarvest());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, harvest));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningEnterSearchLeavesLibraryUnchanged() {
        setupLibrary();
        List<?> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        setupAndCast();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEqualTo(originalLibrary);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void acceptingSearchWithoutBasicLandsFinishesNormally() {
        harness.setLibrary(player1, List.of(new BakersbaneDuo()));
        setupAndCast();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void sacrificeForAnotherReasonAlsoOffersSearchWithoutGainingLife() {
        Permanent harvest = harness.addToBattlefieldAndReturn(player1, new HeapedHarvest());
        harness.setLife(player1, 10);
        setupLibrary();

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, harvest);
            harness.getTriggerCollectionService().checkAllyPermanentSacrificedTriggers(gd, player1.getId(), harvest.getCard());
        });
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Heaped Harvest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
    }

    @Test
    void decliningSacrificeSearchStillGainsLife() {
        harness.addToBattlefield(player1, new HeapedHarvest());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 10);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertInGraveyard(player1, "Heaped Harvest");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @CardUsed(RestInPeace.class)
    void sacrificeStillTriggersWhenRestInPeaceExilesHarvest() {
        harness.addToBattlefield(player1, new HeapedHarvest());
        harness.addToBattlefield(player2, new RestInPeace());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new HeapedHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new BakersbaneDuo()));
    }
}
