package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Clickslither.class, GoblinPiker.class, GrizzlyBears.class})
class ClickslitherTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Goblin gives Clickslither +2/+2 and trample until end of turn")
    void sacrificingGoblinBoostsClickslitherAndGrantsTrample() {
        Permanent clickslither = addClickslitherReady(player1);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(clickslither).doesNotContain(goblin);
        assertThat(clickslither.getEffectivePower()).isEqualTo(5);
        assertThat(clickslither.getEffectiveToughness()).isEqualTo(5);
        assertThat(clickslither.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Clickslither's boost and trample wear off at end of turn")
    void boostAndTrampleWearOffAtEndOfTurn() {
        Permanent clickslither = addClickslitherReady(player1);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(clickslither.getEffectivePower()).isEqualTo(3);
        assertThat(clickslither.getEffectiveToughness()).isEqualTo(3);
        assertThat(clickslither.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The sacrifice cost only accepts Goblin creatures")
    void sacrificeCostOnlyAcceptsGoblinCreatures() {
        addClickslitherReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addClickslitherReady(Player player) {
        Permanent permanent = new Permanent(new Clickslither());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
