package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TVABureaucrat.class, LightningBolt.class})
class TVABureaucratTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell boosts TVA Bureaucrat and makes it unblockable")
    void noncreatureSpellBoostsAndMakesUnblockable() {
        Permanent bureaucrat = addBureaucrat();
        int initialPower = bureaucrat.getEffectivePower();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(bureaucrat.getEffectivePower()).isEqualTo(initialPower + 1);
        assertThat(bureaucrat.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger TVA Bureaucrat")
    void creatureSpellDoesNotTrigger() {
        Permanent bureaucrat = addBureaucrat();

        harness.setHand(player1, List.of(new TVABureaucrat()));
        harness.addMana(player1, ManaColor.BLUE, 2);

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

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(bureaucrat.getEffectivePower()).isEqualTo(2);
        assertThat(bureaucrat.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bureaucrat.getEffectivePower()).isEqualTo(1);
        assertThat(bureaucrat.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The cast trigger resolves before the noncreature spell")
    void triggerResolvesBeforeSpell() {
        Permanent bureaucrat = addBureaucrat();
        int initialPower = bureaucrat.getEffectivePower();
        int initialToughness = bureaucrat.getEffectiveToughness();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(bureaucrat.getEffectivePower()).isEqualTo(initialPower);
        assertThat(bureaucrat.isCantBeBlocked()).isFalse();

        harness.passBothPriorities();

        assertThat(bureaucrat.getEffectivePower()).isEqualTo(initialPower + 1);
        assertThat(bureaucrat.getEffectiveToughness()).isEqualTo(initialToughness);
        assertThat(bureaucrat.isCantBeBlocked()).isTrue();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Each noncreature spell adds another boost in the same turn")
    void multipleNoncreatureSpellsStackBoosts() {
        Permanent bureaucrat = addBureaucrat();
        int initialPower = bureaucrat.getEffectivePower();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(bureaucrat.getEffectivePower()).isEqualTo(initialPower + 2);
        assertThat(bureaucrat.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bureaucrat.getEffectivePower()).isEqualTo(initialPower);
        assertThat(bureaucrat.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger TVA Bureaucrat")
    void opponentNoncreatureSpellDoesNotTrigger() {
        Permanent bureaucrat = addBureaucrat();
        int initialPower = bureaucrat.getEffectivePower();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(bureaucrat.getEffectivePower()).isEqualTo(initialPower);
        assertThat(bureaucrat.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The controller's noncreature spell also triggers during an opponent's turn")
    void controllerSpellTriggersOnOpponentTurn() {
        Permanent bureaucrat = addBureaucrat();
        int initialPower = bureaucrat.getEffectivePower();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(bureaucrat.getEffectivePower()).isEqualTo(initialPower + 1);
        assertThat(bureaucrat.isCantBeBlocked()).isTrue();
    }

    private Permanent addBureaucrat() {
        Permanent bureaucrat = addCreatureReady(player1, new TVABureaucrat());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return bureaucrat;
    }
}
