package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.RatOut;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HopelessNightmare.class, RatOut.class})
class HopelessNightmareTest extends BaseCardTest {

    @Test
    @DisplayName("Entering makes each opponent discard a card and lose 2 life")
    void entersMakesOpponentsDiscardAndLoseLife() {
        harness.setHand(player2, new ArrayList<>(List.of(new RatOut())));
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .contains("Rat Out");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Entering still makes an opponent with an empty hand lose 2 life")
    void entersWithEmptyOpponentHand() {
        harness.setHand(player2, new ArrayList<>());
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Paying {2}{B} sacrifices it and triggers scry 2")
    void sacrificeAbilitySacrificesItAndScries() {
        castAndResolve();
        Card first = new RatOut();
        Card second = new HopelessNightmare();
        Card third = new RatOut();
        harness.setLibrary(player1, List.of(first, second, third));
        Permanent nightmare = findPermanent(player1, "Hopeless Nightmare");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(nightmare), 0, null, null);

        assertThat(findPermanent(player1, "Hopeless Nightmare")).isSameAs(nightmare);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Hopeless Nightmare")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Hopeless Nightmare");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
    }

    @Test
    @DisplayName("The sacrifice ability can be activated again before it resolves")
    void canActivateAgainInResponse() {
        harness.addToBattlefield(player1, new HopelessNightmare());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(findPermanents(player1, "Hopeless Nightmare")).hasSize(1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(2);
        assertThat(findPermanents(player1, "Hopeless Nightmare")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discarding Hopeless Nightmare does not trigger scry")
    void discardingDoesNotTriggerScry() {
        harness.setHand(player2, List.of(new HopelessNightmare(), new RatOut()));
        castAndResolve();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName).containsExactly("Rat Out");
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Hopeless Nightmare");
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new HopelessNightmare(), "{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
