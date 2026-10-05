package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Recollect;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuintoriusHistoryChaser.class, Recollect.class, Reminisce.class, Shock.class, GrizzlyBears.class})
class QuintoriusHistoryChaserTest extends BaseCardTest {

    @Test
    void createsOneSpiritWhenACardReturnsFromGraveyard() {
        addReadyQuintorius(player1, 5);
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Recollect()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    void optionalDiscardDrawsTwoAndMillsOne() {
        Permanent quintorius = addReadyQuintorius(player1, 5);
        Card discarded = new Shock();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        Card milled = new Shock();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, milled));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded, milled);
        assertThat(quintorius.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void minusFourGrantsKeywordToOwnSpiritsOnlyUntilEndOfTurn() {
        addReadyQuintorius(player1, 5);
        Permanent ownSpirit = addCreatureReady(player1, creatureToken("Own Spirit", 1, 1, true));
        Permanent ownBear = addCreatureReady(player1, creatureToken("Own Bear", 1, 1, false));
        Permanent opponentSpirit = addCreatureReady(player2, creatureToken("Opponent Spirit", 1, 1, true));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ownSpirit.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(ownSpirit.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(ownBear.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(opponentSpirit.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(ownSpirit.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(ownSpirit.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void drawAndMillFinishBeforeAnotherPlayerCanRespondToTheDiscard() {
        addReadyQuintorius(player1, 5);
        Card discarded = new Shock();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        Card milled = new Shock();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, milled));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded, milled);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningDiscardDoesNotDrawOrMillButStillAddsLoyalty() {
        Permanent quintorius = addReadyQuintorius(player1, 5);
        Card retained = new Shock();
        Card top = new GrizzlyBears();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(quintorius.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void acceptingWithAnEmptyHandDoesNotDrawOrMill() {
        Permanent quintorius = addReadyQuintorius(player1, 5);
        Card top = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(quintorius.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void simultaneousGraveyardDeparturesCreateOnlyOneSpirit() {
        addReadyQuintorius(player1, 5);
        Card first = new Shock();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(first, second);
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    void opponentGraveyardDeparturesDoNotCreateSpirits() {
        addReadyQuintorius(player1, 5);
        Card returned = new Shock();
        harness.setGraveyard(player2, List.of(returned));
        harness.setHand(player2, List.of(new Recollect()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, returned.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(returned);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }
    private Permanent addReadyQuintorius(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new QuintoriusHistoryChaser());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }

    private Card creatureToken(String name, int power, int toughness, boolean spirit) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setToken(true);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(spirit ? com.github.laxika.magicalvibes.model.CardSubtype.SPIRIT
                : com.github.laxika.magicalvibes.model.CardSubtype.BEAR));
        return card;
    }
}
