package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsecrateConsume.class, GrizzlyBears.class, HillGiant.class})
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

        harness.castAndResolveSorcery(player1, 0, CONSUME, player2.getId());

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

        harness.castAndResolveSorcery(player1, 0, CONSUME, player2.getId());

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

    @Test
    void consumeCannotBeCastDuringCombat() {
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ConsecrateConsume()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, CONSUME, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void consumeWithNoCreaturesGainsNoLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ConsecrateConsume()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, CONSUME, player2.getId());

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Consecrate // Consume");
    }

    @Test
    void consumeCanTargetItsController() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ConsecrateConsume()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, CONSUME, player1.getId());

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 13);
    }

    @Test
    void consecrateCanExileOwnCardWithBlackManaDuringCombat() {
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.setHand(player1, List.of(new ConsecrateConsume()));
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, CONSECRATE, graveyardCard.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(graveyardCard);
        harness.assertInHand(player1, "Hill Giant");
    }

    @Test
    void consecrateDoesNotDrawWhenItsTargetLeavesTheGraveyard() {
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.setHand(player1, List.of(new ConsecrateConsume()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, CONSECRATE, graveyardCard.getId());
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(graveyardCard));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Consecrate // Consume");
    }
}
