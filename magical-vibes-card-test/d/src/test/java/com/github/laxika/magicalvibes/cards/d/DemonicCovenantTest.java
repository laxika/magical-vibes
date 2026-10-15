package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BruvacTheGrandiloquent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.s.ScourgeOfNumai;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonicCovenant.class, ScourgeOfNumai.class, GrizzlyBears.class, Forest.class,
        Ornithopter.class, BruvacTheGrandiloquent.class, RestInPeace.class})
class DemonicCovenantTest extends BaseCardTest {

    @Test
    @DisplayName("Demons attacking a player draw a card and lose 1 life once per attacked player")
    void demonsAttackingDrawAndLoseLifeOncePerPlayer() {
        harness.addToBattlefield(player1, new DemonicCovenant());
        Permanent firstDemon = addCreatureReady(player1, new ScourgeOfNumai());
        Permanent secondDemon = addCreatureReady(player1, new ScourgeOfNumai());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstDemon),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondDemon)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Non-Demons do not trigger the attack ability")
    void nonDemonsDoNotTriggerAttackAbility() {
        harness.addToBattlefield(player1, new DemonicCovenant());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The end step creates a Demon, mills two cards, and sacrifices on an exact type match")
    void endStepCreatesAndSacrificesOnExactTypeMatch() {
        Permanent covenant = harness.addToBattlefieldAndReturn(player1, new DemonicCovenant());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        advanceToEndStepAndResolve(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(covenant);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Cards with different type combinations do not sacrifice the enchantment")
    void endStepKeepsCovenantWhenTypeCombinationsDiffer() {
        Permanent covenant = harness.addToBattlefieldAndReturn(player1, new DemonicCovenant());
        Card first = new Ornithopter();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        advanceToEndStepAndResolve(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(covenant);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("An empty library still creates the specified Demon without sacrificing the enchantment")
    void emptyLibraryStillCreatesDemon() {
        Permanent covenant = harness.addToBattlefieldAndReturn(player1, new DemonicCovenant());
        harness.setLibrary(player1, List.of());

        advanceToEndStepAndResolve(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(covenant);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        Card token = tokens.getFirst().getCard();
        assertThat(token.getPower()).isEqualTo(5);
        assertThat(token.getToughness()).isEqualTo(5);
        assertThat(token.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getSubtypes()).contains(CardSubtype.DEMON);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Milling only one available card does not sacrifice the enchantment")
    void singleAvailableCardDoesNotSacrifice() {
        Permanent covenant = harness.addToBattlefieldAndReturn(player1, new DemonicCovenant());
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));

        advanceToEndStepAndResolve(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(covenant);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("The enchantment does not trigger during the opponent's end step")
    void opponentEndStepDoesNotTrigger() {
        Permanent covenant = harness.addToBattlefieldAndReturn(player1, new DemonicCovenant());
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        advanceToEndStepAndResolve(player2);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(covenant);
    }

    @Test
    @DisplayName("Different creature subtypes do not prevent sacrifice when card types match")
    void differentSubtypesStillSacrifice() {
        Permanent covenant = harness.addToBattlefieldAndReturn(player1, new DemonicCovenant());
        Card first = new GrizzlyBears();
        Card second = new ScourgeOfNumai();
        harness.setLibrary(player1, List.of(first, second));

        advanceToEndStepAndResolve(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, covenant.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(covenant);
    }

    @Test
    @DisplayName("Matching combinations of multiple card types cause sacrifice")
    void matchingArtifactCreatureTypesSacrifice() {
        Permanent covenant = harness.addToBattlefieldAndReturn(player1, new DemonicCovenant());
        Card first = new Ornithopter();
        Card second = new Ornithopter();
        harness.setLibrary(player1, List.of(first, second));

        advanceToEndStepAndResolve(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, covenant.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(covenant);
    }

    @Test
    @DisplayName("Matching milled cards redirected to face-up exile still cause sacrifice")
    void matchingCardsExiledInsteadOfGraveyardStillSacrifice() {
        harness.addToBattlefield(player2, new RestInPeace());
        Permanent covenant = harness.addToBattlefieldAndReturn(player1, new DemonicCovenant());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        advanceToEndStepAndResolve(player1);

        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .contains(first, second, covenant.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(covenant);
    }

    @Test
    @DisplayName("A matching pair later in a doubled mill causes sacrifice")
    void matchingPairBeyondFirstTwoMilledCardsSacrifices() {
        harness.addToBattlefield(player2, new BruvacTheGrandiloquent());
        Permanent covenant = harness.addToBattlefieldAndReturn(player1, new DemonicCovenant());
        Card first = new Forest();
        Card second = new Ornithopter();
        Card third = new GrizzlyBears();
        Card fourth = new ScourgeOfNumai();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        advanceToEndStepAndResolve(player1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first, second, third, fourth, covenant.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(covenant);
    }

    @Test
    @DisplayName("Opposing Demons attacking do not trigger the enchantment")
    void opponentDemonsDoNotTrigger() {
        harness.addToBattlefield(player1, new DemonicCovenant());
        Permanent demon = addCreatureReady(player2, new ScourgeOfNumai());
        harness.setHand(player1, List.of());
        Card draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        harness.setLife(player1, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player2,
                List.of(gd.playerBattlefields.get(player2.getId()).indexOf(demon))));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        harness.assertLife(player1, 20);
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
