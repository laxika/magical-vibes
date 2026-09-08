package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TamiyoCollectorOfTales.class, Forest.class, MindRot.class, CruelEdict.class, GrizzlyBears.class})
class TamiyoCollectorOfTalesTest extends BaseCardTest {

    @Test
    @DisplayName("+1 names a nonland card and puts matching revealed cards into hand")
    void namesNonlandAndReturnsMatches() {
        Card hit1 = nonland("Chosen Card");
        Card miss = nonland("Missed Card");
        Card hit2 = nonland("Chosen Card");
        Card land = new Forest();
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(hit1, miss, hit2, land)));

        Permanent tamiyo = addTamiyo(4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ChooseNonlandCardNameRevealTopCardsChoice.class);
        assertThat(choice.options()).contains("Chosen Card").doesNotContain("Forest");

        harness.handleListChoice(player1, "Chosen Card");

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(hit1.getId(), hit2.getId())
                .doesNotContain(miss.getId(), land.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(miss.getId(), land.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(tamiyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-3 returns a target card from the graveyard to hand")
    void returnsTargetCardFromGraveyard() {
        Card returned = nonland("Returned Card");
        Card remaining = nonland("Remaining Card");
        harness.setGraveyard(player1, List.of(returned, remaining));

        addTamiyo(3);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(returned.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(returned.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(remaining.getId())
                .doesNotContain(returned.getId());
    }

    @Test
    @DisplayName("Prevents an opponent's discard effect from making the controller discard")
    void preventsOpponentDiscard() {
        addTamiyo(4);
        Card first = nonland("First Card");
        Card second = nonland("Second Card");
        harness.setHand(player1, List.of(first, second));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new MindRot()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(first.getId(), second.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Prevents an opponent's sacrifice effect from making the controller sacrifice")
    void preventsOpponentSacrifice() {
        addTamiyo(4);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tamiyo, Collector of Tales");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addTamiyo(int loyalty) {
        Permanent tamiyo = harness.addToBattlefieldAndReturn(player1, new TamiyoCollectorOfTales());
        tamiyo.setCounterCount(CounterType.LOYALTY, loyalty);
        tamiyo.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return tamiyo;
    }

    private static Card nonland(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(CardColor.BLUE);
        return card;
    }
}
