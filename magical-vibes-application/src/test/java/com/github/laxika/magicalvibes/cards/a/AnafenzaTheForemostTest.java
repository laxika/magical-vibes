package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AnafenzaTheForemostTest extends BaseCardTest {

    @Test
    @DisplayName("Attacks and targets another tapped creature you control for a counter")
    void attacksAndCountersAnotherTappedCreatureYouControl() {
        Permanent anafenza = addReadyCreature(player1, new AnafenzaTheForemost());
        Permanent tappedCreature = addReadyCreature(player1, new GrizzlyBears());
        tappedCreature.tap();
        Permanent untappedCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentCreature = addReadyCreature(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(tappedCreature.getId())
                .doesNotContain(anafenza.getId(), untappedCreature.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player1, tappedCreature.getId());
        harness.passBothPriorities();

        assertThat(tappedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiles an opponent-owned nontoken creature instead of letting it die")
    void opponentOwnedNontokenCreatureIsExiledInsteadOfDying() {
        harness.addToBattlefield(player1, new AnafenzaTheForemost());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("Does not exile a creature its controller owns")
    void ownCreatureStillGoesToGraveyard() {
        harness.addToBattlefield(player1, new AnafenzaTheForemost());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isFalse();
    }

    @Test
    @DisplayName("Does not exile a token creature")
    void tokenCreatureIsNotExiled() {
        harness.addToBattlefield(player1, new AnafenzaTheForemost());
        Permanent token = addTokenCreature(player2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, token.getId());
        harness.passBothPriorities();

        assertThat(isExiled("Bear Token")).isFalse();
    }

    @Test
    @DisplayName("Exiles an opponent's discarded creature card but not a noncreature card")
    void opponentCreatureCardDiscardedFromHandIsExiled() {
        harness.addToBattlefield(player1, new AnafenzaTheForemost());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Peek");
        assertThat(isExiled("Grizzly Bears")).isTrue();
        assertThat(isExiled("Peek")).isFalse();
    }

    private Permanent addTokenCreature(Player player) {
        Card tokenCard = new Card();
        tokenCard.setName("Bear Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(CardColor.GREEN);
        tokenCard.setPower(2);
        tokenCard.setToughness(2);
        tokenCard.setSubtypes(List.of(CardSubtype.BEAR));
        Permanent token = new Permanent(tokenCard);
        gd.playerBattlefields.get(player.getId()).add(token);
        return token;
    }

    private boolean isExiled(String cardName) {
        return gd.exiledCards.stream().anyMatch(exiled -> exiled.card().getName().equals(cardName));
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
