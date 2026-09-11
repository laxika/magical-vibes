package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AgentMariaHill;
import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainAmericaWingsOfFreedom.class, AgentMariaHill.class, GoblinHero.class})
class CaptainAmericaWingsOfFreedomTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts other Heroes you control by Captain America's toughness")
    void attackBoostsOtherHeroesYouControl() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaWingsOfFreedom());
        Permanent ownHero = addCreatureReady(player1, new AgentMariaHill());
        Permanent ownNonHero = addCreatureReady(player1, new GoblinHero());
        Permanent opponentHero = addCreatureReady(player2, new AgentMariaHill());
        captain.setToughnessModifier(2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownHero)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownHero)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownNonHero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownNonHero)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentHero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentHero)).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new CaptainAmericaWingsOfFreedom());
        Permanent ownHero = addCreatureReady(player1, new AgentMariaHill());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ownHero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownHero)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownHero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownHero)).isEqualTo(1);
    }
}
