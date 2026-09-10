package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KayaGeistHunter.class, BladeSplicer.class, GrizzlyBears.class, Forest.class, Shock.class})
class KayaGeistHunterTest extends BaseCardTest {

    @Test
    @DisplayName("+1 grants deathtouch to your creatures and puts a counter on a target creature token")
    void plusOneGrantsDeathtouchAndCounter() {
        Permanent kaya = addReadyKaya(3);
        harness.enterBattlefieldAndReturn(player1, new BladeSplicer());
        resolveAllTriggers();

        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, golem.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.DEATHTOUCH)).isTrue();
        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 can be activated without choosing a token")
    void plusOneAllowsNoTarget() {
        Permanent kaya = addReadyKaya(3);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("-2 doubles tokens created under your control until end of turn")
    void minusTwoDoublesTokensUntilEndOfTurn() {
        Permanent kaya = addReadyKaya(4);

        harness.activateAbility(player1, battlefieldIndex(kaya), 1, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(kaya);

        harness.enterBattlefieldAndReturn(player1, new BladeSplicer());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Phyrexian Golem")).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.enterBattlefieldAndReturn(player1, new BladeSplicer());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Phyrexian Golem")).isEqualTo(3);
    }

    @Test
    @DisplayName("-6 exiles all graveyards and creates one Spirit for each exiled card")
    void minusSixExilesGraveyardsAndCreatesSpirits() {
        Permanent kaya = addReadyKaya(7);
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setGraveyard(player2, List.of(instant));

        harness.activateAbility(player1, battlefieldIndex(kaya), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(instant);
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Spirit"), Keyword.FLYING)).isTrue();
    }

    private Permanent addReadyKaya(int loyalty) {
        Permanent kaya = new Permanent(new KayaGeistHunter());
        kaya.setCounterCount(CounterType.LOYALTY, loyalty);
        kaya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kaya);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kaya;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
