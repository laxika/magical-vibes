package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LinebreakerBaloth.class, GrizzlyBears.class, HillGiant.class})
class LinebreakerBalothTest extends BaseCardTest {

    @Test
    @DisplayName("Linebreaker Baloth cannot be blocked by a creature with power 2 or less")
    void cannotBeBlockedByPowerTwoOrLess() {
        Permanent baloth = attackingBaloth();
        gd.playerBattlefields.get(player1.getId()).add(baloth);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Linebreaker Baloth can be blocked by a creature with power 3 or greater")
    void canBeBlockedByPowerThreeOrGreater() {
        Permanent baloth = attackingBaloth();
        gd.playerBattlefields.get(player1.getId()).add(baloth);

        Permanent giant = new Permanent(new HillGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Enlist taps a nonattacking creature and boosts Linebreaker Baloth by its power")
    void enlistBoostsAttackerBySupporterPower() {
        Permanent baloth = addCreatureReady(player1, new LinebreakerBaloth());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isTrue();
        assertThat(baloth.getPowerModifier()).isEqualTo(2);
        assertThat(baloth.getToughnessModifier()).isZero();
    }

    private Permanent attackingBaloth() {
        Permanent baloth = new Permanent(new LinebreakerBaloth());
        baloth.setSummoningSick(false);
        baloth.setAttacking(true);
        return baloth;
    }
}
