package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadrixSilverquill.class, EagerFirstYear.class})
class ShadrixSilverquillTest extends BaseCardTest {

    private static final String TOKEN_MODE =
            "Target player creates a 2/1 white and black Inkling creature token with flying";
    private static final String DRAW_MODE = "Target player draws a card and loses 1 life";
    private static final String COUNTER_MODE =
            "Target player puts a +1/+1 counter on each creature they control";

    @Test
    @DisplayName("Chooses two modes and applies them to different players")
    void choosesTwoModesForDifferentPlayers() {
        harness.addToBattlefield(player1, new ShadrixSilverquill());
        int playerTwoHandSize = gd.playerHands.get(player2.getId()).size();

        advanceToCombat(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, TOKEN_MODE);
        harness.handleListChoice(player1, DRAW_MODE);

        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        Permanent inkling = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(inkling.getCard().getName()).isEqualTo("Inkling");
        assertThat(inkling.getCard().getPower()).isEqualTo(2);
        assertThat(inkling.getCard().getToughness()).isEqualTo(1);
        assertThat(inkling.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(inkling.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(inkling.getCard().getSubtypes()).contains(CardSubtype.INKLING);
        assertThat(inkling.getCard().getKeywords())
                .contains(com.github.laxika.magicalvibes.model.Keyword.FLYING);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(playerTwoHandSize + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The counter mode puts counters on creatures controlled by its target")
    void counterModeTargetsAnotherPlayerThanDrawMode() {
        harness.addToBattlefield(player1, new ShadrixSilverquill());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        int playerTwoHandSize = gd.playerHands.get(player2.getId()).size();

        advanceToCombat(player1);
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handleListChoice(player1, DRAW_MODE);

        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(playerTwoHandSize + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Choosing no modes leaves the triggered ability without effects")
    void mayChooseNoModes() {
        harness.addToBattlefield(player1, new ShadrixSilverquill());

        advanceToCombat(player1);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains(ChooseOneEffect.NO_MODE_LABEL);
        harness.handleListChoice(player1, ChooseOneEffect.NO_MODE_LABEL);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new ShadrixSilverquill());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The second mode cannot target the player already targeted by the first mode")
    void modesRequireDifferentPlayers() {
        harness.addToBattlefield(player1, new ShadrixSilverquill());

        advanceToCombat(player1);
        harness.handleListChoice(player1, TOKEN_MODE);
        harness.handleListChoice(player1, DRAW_MODE);
        harness.handlePermanentChosen(player1, player1.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).contains(player2.getId()).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("Token and counter modes affect only their respective target players")
    void tokenAndCounterModesAffectDifferentPlayers() {
        Permanent shadrix = harness.addToBattlefieldAndReturn(player1, new ShadrixSilverquill());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear());

        advanceToCombat(player1);
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handleListChoice(player1, TOKEN_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(shadrix.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent inkling = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(inkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter mode can target a player who controls no creatures")
    void counterModeCanTargetEmptyBattlefield() {
        harness.addToBattlefield(player1, new ShadrixSilverquill());

        advanceToCombat(player1);
        harness.handleListChoice(player1, TOKEN_MODE);
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
