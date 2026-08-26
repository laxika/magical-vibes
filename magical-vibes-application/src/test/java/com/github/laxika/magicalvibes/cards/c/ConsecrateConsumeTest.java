package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsecrateConsumeTest extends BaseCardTest {

    private static final int CONSECRATE = 0;
    private static final int CONSUME = 1;

    @Test
    @DisplayName("Consecrate exiles a graveyard card and draws a card")
    void consecrateExilesAndDraws() {
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.setHand(player1, List.of(new ConsecrateConsume()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, CONSECRATE, graveyardCard.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Hill Giant");
    }

    @Test
    @DisplayName("Consume sacrifices the greatest-power creature and gains that much life")
    void consumeSacrificesGreatestPowerAndGainsLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ConsecrateConsume()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, CONSUME, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Consume lets the target player choose among tied greatest-power creatures")
    void consumeAllowsGreatestPowerTieChoice() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent firstGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent secondGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ConsecrateConsume()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, CONSUME, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstGiant.getId(), secondGiant.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(firstGiant.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(secondGiant.getId())
                .doesNotContain(firstGiant.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Consume cannot target a permanent")
    void consumeCannotTargetPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConsecrateConsume()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID permanentId = bears.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, CONSUME, permanentId))
                .isInstanceOf(IllegalStateException.class);
    }
}
