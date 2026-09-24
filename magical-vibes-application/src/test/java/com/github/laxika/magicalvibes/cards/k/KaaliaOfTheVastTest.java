package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DemonOfDeathsGate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaaliaOfTheVast.class, SerraAngel.class, DemonOfDeathsGate.class, ShivanDragon.class,
        GrizzlyBears.class})
class KaaliaOfTheVastTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking an opponent offers an Angel, Demon, or Dragon from hand")
    void offersAngelDemonOrDragonFromHand() {
        addCreatureReady(player1, new KaaliaOfTheVast());
        harness.setHand(player1, List.of(
                new SerraAngel(), new DemonOfDeathsGate(), new ShivanDragon(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0, 1, 2);

        harness.handleCardChosen(player1, 2);

        Permanent dragon = findPermanent(player1, "Shivan Dragon");
        assertThat(dragon.isTapped()).isTrue();
        assertThat(dragon.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Non-Angel, non-Demon, and non-Dragon creature cards are not offered")
    void doesNotOfferOtherCreatureCards() {
        addCreatureReady(player1, new KaaliaOfTheVast());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining leaves the eligible card in hand")
    void decliningLeavesCardInHand() {
        addCreatureReady(player1, new KaaliaOfTheVast());
        harness.setHand(player1, List.of(new SerraAngel()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Serra Angel");
    }
}
