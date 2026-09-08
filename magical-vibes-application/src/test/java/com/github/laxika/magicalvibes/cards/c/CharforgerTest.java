package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CharforgerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Phyrexian Goblin token when it enters")
    void createsPhyrexianGoblinTokenOnEntry() {
        harness.setHand(player1, List.of(new Charforger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Phyrexian Goblin");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Gets oil counters when a creature or artifact you control dies")
    void getsOilCountersWhenOwnCreatureOrArtifactDies() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());

        putIntoGraveyard(creature);
        putIntoGraveyard(artifact);

        assertThat(charforger.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ignores an opponent's permanent and a noncreature nonartifact permanent")
    void ignoresOpponentPermanentAndNoncreatureNonartifact() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        putIntoGraveyard(opponentCreature);
        putIntoGraveyard(land);

        assertThat(charforger.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Removes three oil counters and exiles the top card for play this turn")
    void removesOilCountersAndExilesTopCard() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        charforger.setCounterCount(CounterType.OIL, 3);
        Card top = putCardOnTopOfLibrary();
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(charforger.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
    }

    @Test
    @DisplayName("Cannot activate without three oil counters")
    void cannotActivateWithoutThreeOilCounters() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        charforger.setCounterCount(CounterType.OIL, 2);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }

    private Card putCardOnTopOfLibrary() {
        Card card = new Card();
        card.setName("Exiled Spell");
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}{R}");
        card.setColor(CardColor.RED);
        gd.playerDecks.get(player1.getId()).addFirst(card);
        return card;
    }

    private void enterMainWithPriority(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
