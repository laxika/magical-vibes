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

    private void addManaForX(int xValue) {
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
