package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RotHulk.class, Gravecrawler.class, GrizzlyBears.class})
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

    private void castRotHulk(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new RotHulk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
