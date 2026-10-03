package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.ValakutTheMoltenPinnacle;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelebrateTheHarvest.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        Island.class, LlanowarElves.class, Plains.class, Mountain.class, ValakutTheMoltenPinnacle.class, GiantGrowth.class})
class CelebrateTheHarvestTest extends BaseCardTest {

    @Test
    void searchesForOneBasicLandPerDistinctControlledCreaturePower() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        castCelebrateTheHarvest();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(3);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(3)
                .allMatch(com.github.laxika.magicalvibes.model.Permanent::isTapped);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void countsDistinctPowersRatherThanCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island()));
        castCelebrateTheHarvest();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void ignoresOpponentsCreaturesWhenControllerHasNone() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));
        castCelebrateTheHarvest();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Celebrate the Harvest");
    }

    @Test
    void canChooseNoLandsEvenWhenBasicLandsAreAvailable() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));
        castCelebrateTheHarvest();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Celebrate the Harvest");
    }

    @Test
    void canStopAfterFindingFewerLandsThanTheNumberOfPowers() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));
        castCelebrateTheHarvest();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(1)
                .allMatch(com.github.laxika.magicalvibes.model.Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Celebrate the Harvest");
    }

    @Test
    void finishesWhenFewerBasicLandsExistThanDistinctPowers() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new HillGiant()));
        castCelebrateTheHarvest();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(1)
                .allMatch(com.github.laxika.magicalvibes.model.Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Celebrate the Harvest");
    }

    @Test
    void twoMountainsEnterTogetherAndBothTriggerValakutAtTheThreshold() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.setLife(player2, 20);
        castCelebrateTheHarvest();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        while (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
        int triggerCount = gd.stack.size();
        for (int i = 0; i < triggerCount; i++) {
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertLife(player2, 14);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void countsEffectivePowersAtResolutionAfterAResponse() {
        var bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));
        castCelebrateTheHarvest();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Celebrate the Harvest");
    }

    @Test
    void doesNotFindNonbasicLandsOrCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new ValakutTheMoltenPinnacle(), new LlanowarElves()));
        castCelebrateTheHarvest();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Celebrate the Harvest");
    }

    private void castCelebrateTheHarvest() {
        harness.castFromHand(player1, new CelebrateTheHarvest(), "{3}{G}");
    }
}
