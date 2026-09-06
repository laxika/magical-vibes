package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassProduction;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnafenzaKinTreeSpirit.class, GrizzlyBears.class, Memnite.class, MassProduction.class})
class AnafenzaKinTreeSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken creature entering bolsters a creature with the least toughness")
    void anotherNontokenCreatureEnteringBolstersLeastToughness() {
        harness.addToBattlefield(player1, new AnafenzaKinTreeSpirit());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(memnite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a token entering")
    void doesNotTriggerForTokenEntering() {
        harness.addToBattlefield(player1, new AnafenzaKinTreeSpirit());

        harness.setHand(player1, List.of(new MassProduction()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature entering")
    void doesNotTriggerForOpponentsCreatureEntering() {
        harness.addToBattlefield(player1, new AnafenzaKinTreeSpirit());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when Anafenza enters")
    void doesNotTriggerForSelfEntering() {
        harness.setHand(player1, List.of(new AnafenzaKinTreeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
