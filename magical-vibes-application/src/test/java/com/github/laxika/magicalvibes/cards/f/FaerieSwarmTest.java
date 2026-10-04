package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BallynockCohort;
import com.github.laxika.magicalvibes.cards.c.Cursecatcher;
import com.github.laxika.magicalvibes.cards.p.PucasMischief;
import com.github.laxika.magicalvibes.cards.w.WaspLancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieSwarm.class, BallynockCohort.class, Cursecatcher.class, PucasMischief.class, WaspLancer.class})
class FaerieSwarmTest extends BaseCardTest {
    @Test
    void countsMulticoloredBluePermanentOnce() {
        Permanent swarm = addCreatureReady(player1, new FaerieSwarm());
        harness.addToBattlefield(player1, new WaspLancer());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(2);
    }

    @Test
    void characteristicAbilityWorksInHandWithoutCountingCardsInHand() {
        FaerieSwarm swarm = new FaerieSwarm();
        harness.setHand(player1, List.of(swarm, new Cursecatcher()));
        harness.addToBattlefield(player1, new PucasMischief());
        harness.addToBattlefield(player2, new Cursecatcher());

        assertThat(gqs.getEffectiveCardPower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    void characteristicAbilityWorksInGraveyardWithNoBluePermanents() {
        FaerieSwarm swarm = new FaerieSwarm();
        harness.setGraveyard(player1, List.of(swarm, new Cursecatcher()));
        harness.addToBattlefield(player1, new BallynockCohort());
        harness.addToBattlefield(player2, new Cursecatcher());

        assertThat(gqs.getEffectiveCardPower(gd, swarm)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, swarm)).isZero();
    }

    @Test
    @DisplayName("Counts itself as a blue permanent when alone: 1/1")
    void countsItselfWhenAlone() {
        Permanent swarm = addCreatureReady(player1, new FaerieSwarm());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equals the number of blue permanents you control")
    void ptEqualsBluePermanents() {
        Permanent swarm = addCreatureReady(player1, new FaerieSwarm());
        harness.addToBattlefield(player1, new Cursecatcher());
        harness.addToBattlefield(player1, new PucasMischief());

        // itself + 1 blue creature + 1 blue enchantment = 3
        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-blue permanents are not counted")
    void nonBlueNotCounted() {
        Permanent swarm = addCreatureReady(player1, new FaerieSwarm());
        harness.addToBattlefield(player1, new BallynockCohort());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only counts your blue permanents, not the opponent's")
    void countsOnlyControllersPermanents() {
        Permanent swarm = addCreatureReady(player1, new FaerieSwarm());
        harness.addToBattlefield(player2, new Cursecatcher());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when blue permanents change")
    void ptUpdatesWhenBluePermanentsChange() {
        Permanent swarm = addCreatureReady(player1, new FaerieSwarm());
        Permanent bluePermanent = harness.addToBattlefieldAndReturn(player1, new Cursecatcher());
        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(bluePermanent);
        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }
}
