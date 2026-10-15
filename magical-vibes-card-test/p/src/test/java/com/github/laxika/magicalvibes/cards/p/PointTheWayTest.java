package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AutarchMammoth;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PointTheWay.class, Forest.class, Island.class, Plains.class, AutarchMammoth.class})
class PointTheWayTest extends BaseCardTest {

    @Test
    void searchesForUpToCurrentSpeedBasicLandsTapped() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PointTheWay());
        gd.playerSpeeds.put(player1.getId(), 2);

        Forest forest = new Forest();
        Island island = new Island();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest, island, plains, new AutarchMammoth()));

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, island, plains);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).allMatch(Permanent::isTapped);
    }

    @Test
    void castingStartsEngines() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PointTheWay()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Point the Way");
    }

    @Test
    void usesSpeedAtResolutionAndAllowsFindingFewerLands() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new PointTheWay());
        gd.playerSpeeds.put(player1.getId(), 1);
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, null, null);
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().remainingCount()).isEqualTo(4);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
    }

    @Test
    void chosenLandsEnterTogetherAfterSearchChoicesAreComplete() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new PointTheWay());
        gd.playerSpeeds.put(player1.getId(), 2);
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).allMatch(Permanent::isTapped);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
