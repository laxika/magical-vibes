package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WisedraftersWill.class, Fog.class, GrizzlyBears.class})
class WisedraftersWillTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals opponents' hands to its controller")
    void revealsOpponentsHands() {
        harness.addToBattlefield(player1, new WisedraftersWill());
        harness.setHand(player1, List.of(new Fog()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.passPriority(player1);

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"opponentHand\"")
                        && message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("\"opponentHand\"")
                        && message.contains("Fog"));
    }

    @Test
    @DisplayName("Sacrifices to draw a card")
    void sacrificesToDraw() {
        harness.addToBattlefield(player1, new WisedraftersWill());
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wisedrafter's Will");
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Sacrifices to counter a target spell")
    void sacrificesToCounterSpell() {
        harness.addToBattlefield(player1, new WisedraftersWill());
        Fog fog = new Fog();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, fog, "{G}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, fog.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wisedrafter's Will");
        harness.assertInGraveyard(player2, "Fog");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot use the counter ability on a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new WisedraftersWill());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                        player1, 0, 1, null,
                        harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Wisedrafter's Will");
    }

    @Test
    @DisplayName("Sacrifice is paid before drawing and immediately ends hand revelation")
    void sacrificeEndsRevelationBeforeDrawResolves() {
        harness.addToBattlefield(player1, new WisedraftersWill());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Fog()));
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.publishState();
        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"opponentHand\"")
                        && message.contains("Fog"));

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Wisedrafter's Will");
        harness.assertInGraveyard(player1, "Wisedrafter's Will");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.clearMessages();
        harness.publishState();
        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"opponentHand\""));
        assertThat(harness.getConn1().getSentMessages())
                .noneMatch(message -> message.contains("Fog"));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Can counter its controller's creature spell")
    void countersOwnCreatureSpell() {
        harness.addToBattlefield(player1, new WisedraftersWill());
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, bears.getId());

        harness.assertInGraveyard(player1, "Wisedrafter's Will");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot sacrifice to draw without blue mana")
    void cannotDrawWithoutBlueMana() {
        harness.addToBattlefield(player1, new WisedraftersWill());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Wisedrafter's Will");
        harness.assertNotInGraveyard(player1, "Wisedrafter's Will");
        assertThat(gd.stack).isEmpty();
    }
}
