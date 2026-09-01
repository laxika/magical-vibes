package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Eureka.class, Forest.class, GrizzlyBears.class, LightningBolt.class})
class EurekaTest extends BaseCardTest {

    @Test
    @DisplayName("Repeats controller-first rounds until no player puts a permanent onto the battlefield")
    void repeatsControllerFirstRoundsUntilNoPlayerPutsPermanent() {
        Eureka eureka = new Eureka();
        GrizzlyBears bears = new GrizzlyBears();
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(eureka, bears, firstForest));
        harness.setHand(player2, List.of(secondForest, bolt));

        castEureka();

        PendingInteraction.EachPlayerMayPutCardFromHandChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validCardIds()).containsExactly(bears.getId(), firstForest.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(bears.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class)
                .playerId()).isEqualTo(player2.getId());
        harness.handleMultipleCardsChosen(player2, List.of(secondForest.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(firstForest.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(bears.getId(), firstForest.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(secondForest.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bolt);
        harness.assertInGraveyard(player1, "Eureka");
    }

    @Test
    @DisplayName("Does not offer nonpermanent cards")
    void doesNotOfferNonpermanentCards() {
        Eureka eureka = new Eureka();
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(eureka, bolt));
        harness.setHand(player2, List.of());

        castEureka();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bolt);
        harness.assertInGraveyard(player1, "Eureka");
    }

    private void castEureka() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
