package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JunglebornPioneer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WorldShaper.class, Forest.class, JunglebornPioneer.class})
class WorldShaperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with World Shaper may mill three cards")
    void attackingMayMillThreeCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new WorldShaper());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Declining World Shaper's attack trigger does not mill")
    void decliningAttackTriggerDoesNotMill() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new WorldShaper());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("When World Shaper dies, all land cards in its controller's graveyard return tapped")
    void deathReturnsAllLandsTapped() {
        Card firstForest = new Forest();
        Card secondForest = new Forest();
        Card nonland = new JunglebornPioneer();
        harness.setGraveyard(player1, List.of(firstForest, nonland, secondForest));
        Permanent worldShaper = harness.addToBattlefieldAndReturn(player1, new WorldShaper());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, worldShaper));
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        harness.assertInGraveyard(player1, "Jungleborn Pioneer");
        harness.assertInGraveyard(player1, "World Shaper");
    }

    @Test
    @DisplayName("Accepting the attack trigger with fewer than three cards mills the whole library")
    void attackMillsRemainingCardsFromShortLibrary() {
        Card forest = new Forest();
        Card nonland = new JunglebornPioneer();
        harness.setLibrary(player1, List.of(forest, nonland));
        addCreatureReady(player1, new WorldShaper());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(forest, nonland);
    }

    @Test
    @DisplayName("The attack trigger mills only the top three cards and only its controller's library")
    void attackMillsOnlyTopThreeCards() {
        Card first = new Forest();
        Card second = new JunglebornPioneer();
        Card third = new Forest();
        Card fourth = new Forest();
        Card opponentsCard = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setLibrary(player2, List.of(opponentsCard));
        addCreatureReady(player1, new WorldShaper());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The death trigger returns lands present at resolution and leaves opponents' lands alone")
    void deathReturnsCurrentLandsOnlyFromControllersGraveyard() {
        Card forest = new Forest();
        Card opponentsForest = new Forest();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentsForest));
        Permanent worldShaper = harness.addToBattlefieldAndReturn(player1, new WorldShaper());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, worldShaper));
        harness.setGraveyard(player1, List.of(worldShaper.getCard(), forest));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard()).isSameAs(forest);
            assertThat(permanent.isTapped()).isTrue();
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(worldShaper.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsForest);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The death trigger does nothing when its controller has no land cards in the graveyard")
    void deathWithNoLandsDoesNotReturnNonlands() {
        Card nonland = new JunglebornPioneer();
        harness.setGraveyard(player1, List.of(nonland));
        Permanent worldShaper = harness.addToBattlefieldAndReturn(player1, new WorldShaper());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, worldShaper));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(nonland, worldShaper.getCard());
    }
}
