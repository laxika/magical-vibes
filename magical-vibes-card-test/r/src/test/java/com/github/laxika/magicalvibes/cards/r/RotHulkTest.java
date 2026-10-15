package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RotHulk.class, GrizzlyBears.class, Gravecrawler.class})
class RotHulkTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to one Zombie card from your graveyard")
    void returnsUpToOneZombieCard() {
        Gravecrawler zombie = new Gravecrawler();
        GrizzlyBears nonZombie = new GrizzlyBears();
        castRotHulk(List.of(zombie, nonZombie));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(zombie.getId());

        harness.handleMultipleCardsChosen(player1, List.of(zombie.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rot Hulk");
        harness.assertOnBattlefield(player1, "Gravecrawler");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return more Zombie cards than the number of opponents")
    void capsReturnedZombiesAtOpponentCount() {
        Gravecrawler firstZombie = new Gravecrawler();
        Gravecrawler secondZombie = new Gravecrawler();
        castRotHulk(List.of(firstZombie, secondZombie));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstZombie.getId(), secondZombie.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstZombie.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gravecrawler");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Gravecrawler");
    }

    @Test
    void canChooseZeroZombies() {
        Gravecrawler zombie = new Gravecrawler();
        castRotHulk(List.of(zombie));

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rot Hulk");
        harness.assertInGraveyard(player1, "Gravecrawler");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void resolvesWithNoEligibleCards() {
        castRotHulk(List.of(new GrizzlyBears()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rot Hulk");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetOpponentsZombies() {
        Gravecrawler ownZombie = new Gravecrawler();
        Gravecrawler opposingZombie = new Gravecrawler();
        harness.setGraveyard(player2, List.of(opposingZombie));
        castRotHulk(List.of(ownZombie));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownZombie.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownZombie.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gravecrawler");
        harness.assertInGraveyard(player2, "Gravecrawler");
    }

    @Test
    void doesNotReturnTargetThatLeftTheGraveyard() {
        Gravecrawler zombie = new Gravecrawler();
        castRotHulk(List.of(zombie));
        harness.handleMultipleCardsChosen(player1, List.of(zombie.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(zombie));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rot Hulk");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(zombie);
        assertThat(gd.stack).isEmpty();
    }

    private void castRotHulk(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        harness.castFromHand(player1, new RotHulk(), "{5}{B}{B}");
        harness.passBothPriorities();
    }
}
