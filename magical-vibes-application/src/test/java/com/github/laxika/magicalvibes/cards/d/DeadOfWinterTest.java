package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredSwamp;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadOfWinter.class, GrizzlyBears.class, SnowCoveredSwamp.class})
class DeadOfWinterTest extends BaseCardTest {

    @Test
    @DisplayName("Gives nonsnow creatures -X/-X based on the snow permanents its controller controls")
    void givesNonsnowCreaturesMinusX() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        harness.addToBattlefield(player2, new SnowCoveredSwamp());
        Permanent snowCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        TestCards.mutableCard(snowCreature).setSupertypes(EnumSet.of(CardSupertype.SNOW));

        castDeadOfWinter();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(1);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(snowCreature.getEffectivePower()).isEqualTo(2);
        assertThat(snowCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts multiple snow permanents and kills creatures with zero toughness")
    void countsMultipleSnowPermanents() {
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDeadOfWinter();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The temporary debuff wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SnowCoveredSwamp());

        castDeadOfWinter();
        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    private void castDeadOfWinter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DeadOfWinter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
