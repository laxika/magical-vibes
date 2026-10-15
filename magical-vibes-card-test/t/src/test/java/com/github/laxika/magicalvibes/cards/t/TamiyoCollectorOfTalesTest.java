package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.Card;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TamiyoCollectorOfTales.class, Forest.class, MindRot.class, CruelEdict.class, GrizzlyBears.class})
class TamiyoCollectorOfTalesTest extends BaseCardTest {

    @Test
    @DisplayName("+1 names a nonland card and puts matching revealed cards into hand")
    void namesNonlandAndReturnsMatches() {
        Card hit1 = new MindRot();
        Card miss = new CruelEdict();
        Card hit2 = new MindRot();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(hit1, miss, hit2, land));

        Permanent tamiyo = addTamiyo(4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ChooseNonlandCardNameRevealTopCardsChoice.class);
        assertThat(choice.options()).contains("Mind Rot").doesNotContain("Forest");

        harness.handleListChoice(player1, "Mind Rot");

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
        Card returned = new MindRot();
        Card remaining = new Forest();
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
        Card first = new Forest();
        Card second = new GrizzlyBears();
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

    @Test
    @DisplayName("+1 rejects a land card name without moving library cards")
    void rejectsLandCardName() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        addTamiyo(4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Forest"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("+1 reveals all available cards when fewer than four remain")
    void revealsShortLibrary() {
        Card hit = new TamiyoCollectorOfTales();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(hit, land));
        addTamiyo(4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Tamiyo, Collector of Tales");

        assertThat(gd.playerHands.get(player1.getId())).contains(hit).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land).doesNotContain(hit);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("-3 can return a land card")
    void returnsLandFromGraveyard() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        addTamiyo(4);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("Tamiyo does not prevent her controller's own discard spell")
    void permitsControllerDiscard() {
        addTamiyo(4);
        Card first = new Forest();
        Card second = new Forest();
        harness.setHand(player1, List.of(new MindRot(), first, second));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("+1 puts only the top four cards into the graveyard when none match")
    void millsOnlyTopFourWhenNoCardsMatch() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        Card fifth = new TamiyoCollectorOfTales();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        addTamiyo(4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Tamiyo, Collector of Tales");

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(fifth);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first, second, third, fourth).doesNotContain(fifth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth);
    }
    private Permanent addTamiyo(int loyalty) {
        Permanent tamiyo = harness.addToBattlefieldAndReturn(player1, new TamiyoCollectorOfTales());
        tamiyo.setCounterCount(CounterType.LOYALTY, loyalty);
        tamiyo.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return tamiyo;
    }

}
