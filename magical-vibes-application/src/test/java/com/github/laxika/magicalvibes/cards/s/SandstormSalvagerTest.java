package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SandstormSalvager.class, GrizzlyBears.class})
class SandstormSalvagerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a 3/3 colorless Golem artifact creature token")
    void entersWithGolemToken() {
        Permanent salvager = castSalvager();

        Permanent golem = findPermanent(player1, "Golem");
        assertThat(golem.getCard().isToken()).isTrue();
        assertThat(gqs.isArtifact(gd, golem)).isTrue();
        assertThat(gqs.isCreature(gd, golem)).isTrue();
        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(3);
        assertThat(salvager.getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("Puts counters on creature tokens and grants them trample until end of turn")
    void boostsCreatureTokensOnlyUntilEndOfTurn() {
        Permanent salvager = castSalvager();
        Permanent golem = findPermanent(player1, "Golem");
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        salvager.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(salvager), null, null);
        harness.passBothPriorities();

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isTrue();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent castSalvager() {
        harness.setHand(player1, List.of(new SandstormSalvager()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Sandstorm Salvager");
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
