package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SetessanSkirmisher.class, GloriousAnthem.class, GrizzlyBears.class})
class SetessanSkirmisherTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 when an enchantment you control enters")
    void getsBoostWhenOwnEnchantmentEnters() {
        Permanent skirmisher = harness.addToBattlefieldAndReturn(player1, new SetessanSkirmisher());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skirmisher)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, skirmisher)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger for a creature entering or an enchantment an opponent controls")
    void ignoresNonEnchantmentAndOpponentEnchantment() {
        Permanent skirmisher = harness.addToBattlefieldAndReturn(player1, new SetessanSkirmisher());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skirmisher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skirmisher)).isEqualTo(1);

        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skirmisher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skirmisher)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent skirmisher = harness.addToBattlefieldAndReturn(player1, new SetessanSkirmisher());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skirmisher)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, skirmisher)).isEqualTo(2);
    }
}
