package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YahenniUndyingPartisan.class, CruelEdict.class, GrizzlyBears.class})
class YahenniUndyingPartisanTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's creature dying puts a +1/+1 counter on Yahenni")
    void gainsCounterWhenOpponentCreatureDies() {
        Permanent yahenni = addYahenniReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(yahenni.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Yahenni does not trigger when its controller's creature dies")
    void doesNotTriggerOnOwnCreatureDeath() {
        Permanent yahenni = addYahenniReady(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(yahenni.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Sacrificing another creature grants indestructible until end of turn")
    void sacrificingAnotherCreatureGrantsIndestructible() {
        Permanent yahenni = addYahenniReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, yahenni, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Yahenni cannot sacrifice itself")
    void cannotSacrificeItself() {
        addYahenniReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent yahenni = addYahenniReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, yahenni, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, yahenni, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, but indestructible waits for resolution")
    void sacrificeIsPaidBeforeIndestructibleResolves() {
        Permanent yahenni = harness.addToBattlefieldAndReturn(player1, new YahenniUndyingPartisan());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, yahenni, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(yahenni.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, yahenni, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Yahenni can sacrifice another creature while already indestructible")
    void canActivateAgainWhileIndestructible() {
        Permanent yahenni = addYahenniReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(2);
        assertThat(gqs.hasKeyword(gd, yahenni, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(yahenni.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addYahenniReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new YahenniUndyingPartisan());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
