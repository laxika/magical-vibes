package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ClericOfChillDepths;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaboraxHopesDemise.class, ClericOfChillDepths.class, GrizzlyBears.class})
class TaboraxHopesDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("A dying Cleric puts a counter on Taborax and may draw and lose life")
    void clericDeathDrawsAndLosesLifeWhenAccepted() {
        Permanent taborax = harness.addToBattlefieldAndReturn(player1, new TaboraxHopesDemise());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new ClericOfChillDepths());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        kill(cleric);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(taborax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The Cleric death draw is optional, but the counter is mandatory")
    void clericDeathMayDeclineDraw() {
        Permanent taborax = harness.addToBattlefieldAndReturn(player1, new TaboraxHopesDemise());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new ClericOfChillDepths());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        kill(cleric);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(taborax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A dying non-Cleric puts a counter on Taborax without drawing or losing life")
    void nonClericDeathOnlyAddsCounter() {
        Permanent taborax = harness.addToBattlefieldAndReturn(player1, new TaboraxHopesDemise());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        kill(creature);
        harness.passBothPriorities();

        assertThat(taborax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A token creature does not trigger Taborax")
    void tokenDeathDoesNotTrigger() {
        Permanent taborax = harness.addToBattlefieldAndReturn(player1, new TaboraxHopesDemise());
        Permanent token = addToken();

        kill(token);

        assertThat(taborax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Taborax has lifelink only at five or more plus-one-plus-one counters")
    void lifelinkAtCounterThreshold() {
        Permanent taborax = harness.addToBattlefieldAndReturn(player1, new TaboraxHopesDemise());

        assertThat(gqs.hasKeyword(gd, taborax, Keyword.LIFELINK)).isFalse();

        taborax.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        assertThat(gqs.hasKeyword(gd, taborax, Keyword.LIFELINK)).isTrue();

        taborax.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        assertThat(gqs.hasKeyword(gd, taborax, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Taborax does not trigger for its own death")
    void ownDeathDoesNotTrigger() {
        Permanent taborax = harness.addToBattlefieldAndReturn(player1, new TaboraxHopesDemise());

        kill(taborax);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void kill(Permanent permanent) {
        permanent.setMarkedDamage(10);
        harness.runStateBasedActions();
    }

    private Permanent addToken() {
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
        gd.playerBattlefields.get(player1.getId()).add(token);
        return token;
    }
}
