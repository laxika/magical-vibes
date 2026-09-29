package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwinsOfDiscord.class, Memnite.class, GrizzlyBears.class, LlanowarElves.class})
class TwinsOfDiscordTest extends BaseCardTest {

    @Test
    void bloodthirstPutsCountersOnTwinsWhenAnOpponentWasDealtDamage() {
        gd.recordDamageToPlayer(player2.getId(), 1);

        Permanent twins = harness.enterBattlefieldAndReturn(player1, new TwinsOfDiscord());

        assertThat(twins.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void bloodthirstStaticAbilityPutsCountersOnOtherColorlessCreaturesOnly() {
        harness.enterBattlefieldAndReturn(player1, new TwinsOfDiscord());
        gd.recordDamageToPlayer(player2.getId(), 1);

        Permanent memnite = harness.enterBattlefieldAndReturn(player1, new Memnite());
        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(memnite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackTriggerPreventsChosenManaValueParityFromBlocking() {
        Permanent twins = addCreatureReady(player1, new TwinsOfDiscord());
        Permanent evenBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent oddBlocker = addCreatureReady(player2, new LlanowarElves());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(twins)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "EVEN");

        assertThat(bls.canBlockAttacker(gd, evenBlocker, twins,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, oddBlocker, twins,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
