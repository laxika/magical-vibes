package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WurmwallSweeper.class, GrizzlyBears.class})
class WurmwallSweeperTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 2")
    void entersWithSurveilTwo() {
        GameData gd = harness.getGameData();
        Card top0 = new GrizzlyBears();
        Card top1 = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, top1);
        gd.playerDecks.get(player1.getId()).add(0, top0);

        harness.setHand(player1, List.of(new WurmwallSweeper()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(top0, top1);
        assertThat(surveil.toGraveyard()).isTrue();
    }

    @Test
    @DisplayName("Station uses the tapped creature's power")
    void stationUsesTappedCreaturePower() {
        Permanent sweeper = harness.addToBattlefieldAndReturn(player1, new WurmwallSweeper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sweeper), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(sweeper.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Four charge counters make the Spacecraft an artifact creature with flying")
    void fourChargeCountersUnlockAbilities() {
        Permanent sweeper = harness.addToBattlefieldAndReturn(player1, new WurmwallSweeper());

        sweeper.setCounterCount(CounterType.CHARGE, 3);
        assertThat(gqs.isCreature(gd, sweeper)).isFalse();
        assertThat(gqs.hasKeyword(gd, sweeper, Keyword.FLYING)).isFalse();

        sweeper.setCounterCount(CounterType.CHARGE, 4);
        assertThat(gqs.isCreature(gd, sweeper)).isTrue();
        assertThat(gqs.hasKeyword(gd, sweeper, Keyword.FLYING)).isTrue();
    }
}
