package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WorldsoulsRage.class, Forest.class, Mountain.class, Island.class, GrizzlyBears.class})
class WorldsoulsRageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage and puts up to X lands from hand and graveyard onto the battlefield tapped")
    void dealsDamageAndPutsLandsFromBothZonesTapped() {
        WorldsoulsRage rage = new WorldsoulsRage();
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        Island island = new Island();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(rage, forest, mountain));
        harness.setGraveyard(player1, List.of(island, bears));
        addManaForX(3);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validCardIds()).contains(forest.getId(), mountain.getId(), island.getId())
                .doesNotContain(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), island.getId(), mountain.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(forest, island, mountain);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears, rage).hasSize(2);
    }

    @Test
    @DisplayName("With X equal to zero, it deals no damage and puts no lands onto the battlefield")
    void zeroXDoesNothing() {
        WorldsoulsRage rage = new WorldsoulsRage();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setHand(player1, List.of(rage, forest));
        harness.setGraveyard(player1, List.of(island));
        addManaForX(0);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, rage).hasSize(2);
    }

    @Test
    @DisplayName("Can decline to put any lands onto the battlefield after dealing damage")
    void canDeclineAllLands() {
        WorldsoulsRage rage = new WorldsoulsRage();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setHand(player1, List.of(rage, forest));
        harness.setGraveyard(player1, List.of(island));
        addManaForX(2);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(island, rage);
    }

    @Test
    @DisplayName("Can choose fewer than X lands while leaving unchosen lands in their zones")
    void canChooseFewerThanXLands() {
        WorldsoulsRage rage = new WorldsoulsRage();
        Forest forest = new Forest();
        Island island = new Island();
        Mountain mountain = new Mountain();
        harness.setHand(player1, List.of(rage, forest));
        harness.setGraveyard(player1, List.of(island, mountain));
        addManaForX(3);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(island.getId()));

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(island);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(mountain, rage);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Still deals damage when there are no lands to put onto the battlefield")
    void dealsDamageWithoutAvailableLands() {
        WorldsoulsRage rage = new WorldsoulsRage();
        harness.setHand(player1, List.of(rage));
        addManaForX(2);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rage);
    }

    @Test
    @DisplayName("Does not put lands onto the battlefield if its only target has left the battlefield")
    void illegalTargetPreventsLandPlacement() {
        WorldsoulsRage rage = new WorldsoulsRage();
        Forest forest = new Forest();
        Island island = new Island();
        GrizzlyBears bears = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, bears);
        harness.setHand(player1, List.of(rage, forest));
        harness.setGraveyard(player1, List.of(island));
        addManaForX(2);

        harness.castSorcery(player1, 0, 2, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(island, rage);
    }

    @Test
    @DisplayName("Deals lethal damage to a creature and still puts a land from hand onto the battlefield")
    void lethalCreatureDamageDoesNotPreventLandPlacement() {
        WorldsoulsRage rage = new WorldsoulsRage();
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, bears);
        harness.setHand(player1, List.of(rage, forest));
        addManaForX(2);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears);
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rage);
    }

    @Test
    @DisplayName("Only the controller's lands can be chosen, with a combined limit of X across both zones")
    void limitsChoicesToControllersLandsAndX() {
        WorldsoulsRage rage = new WorldsoulsRage();
        Forest forest = new Forest();
        Island island = new Island();
        Mountain opponentHandLand = new Mountain();
        Forest opponentGraveyardLand = new Forest();
        harness.setHand(player1, List.of(rage, forest));
        harness.setGraveyard(player1, List.of(island));
        harness.setHand(player2, List.of(opponentHandLand));
        harness.setGraveyard(player2, List.of(opponentGraveyardLand));
        addManaForX(1);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(forest.getId(), island.getId());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(island, rage);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentHandLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardLand);
    }

    private void addManaForX(int xValue) {
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
