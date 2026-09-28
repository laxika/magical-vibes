package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TVABureaucrat.class, LightningBolt.class, GrizzlyBears.class})
class TVABureaucratTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell boosts TVA Bureaucrat and makes it unblockable")
    void noncreatureSpellBoostsAndMakesUnblockable() {
        Permanent bureaucrat = addBureaucrat();
        int initialPower = bureaucrat.getEffectivePower();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(bureaucrat.getEffectivePower()).isEqualTo(initialPower + 1);
        assertThat(bureaucrat.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger TVA Bureaucrat")
    void creatureSpellDoesNotTrigger() {
        Permanent bureaucrat = addBureaucrat();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(bureaucrat.getEffectivePower()).isEqualTo(1);
        assertThat(bureaucrat.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The boost and unblockability wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent bureaucrat = addBureaucrat();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(bureaucrat.getEffectivePower()).isEqualTo(2);
        assertThat(bureaucrat.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bureaucrat.getEffectivePower()).isEqualTo(1);
        assertThat(bureaucrat.isCantBeBlocked()).isFalse();
    }

    private Permanent addBureaucrat() {
        Permanent bureaucrat = addCreatureReady(player1, new TVABureaucrat());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return bureaucrat;
    }
}
