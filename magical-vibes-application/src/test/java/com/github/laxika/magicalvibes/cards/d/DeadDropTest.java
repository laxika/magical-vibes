package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeadDropTest extends BaseCardTest {

    @Test
    @DisplayName("Delve pays the generic cost and the target opponent chooses two creatures to sacrifice")
    void delvesAndTargetOpponentSacrificesTwoCreatures() {
        List<Card> graveyard = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new DeadDrop()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        harness.castInstantWithMultipleGraveyardExile(player1, 0, player2.getId(),
                List.of(0, 1, 2, 3, 4, 5, 6, 7, 8));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        List<Permanent> creatures = findPermanents(player2, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player2,
                List.of(creatures.get(0).getId(), creatures.get(1).getId()));

        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player2, "Forest")).hasSize(1);
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }
}
