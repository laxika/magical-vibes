package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrintlifterOoze.class, GrizzlyBears.class})
class PrintlifterOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Turning face up creates a trample Ooze with one counter per other creature")
    void turnsFaceUpCreatesScaledOoze() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        castFaceDown();
        Permanent oozeSource = findPermanent(player1, "Printlifter Ooze");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(oozeSource));
        harness.passBothPriorities();

        Permanent ooze = findPermanent(player1, "Ooze");
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ooze.getEffectivePower()).isEqualTo(2);
        assertThat(ooze.getEffectiveToughness()).isEqualTo(2);
        assertThat(ooze.getCard().getKeywords()).contains(Keyword.TRAMPLE);
        assertThat(ooze.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    @DisplayName("With no other creatures, the zero-power Ooze dies to state-based actions")
    void zeroCounterOozeDies() {
        castFaceDown();
        Permanent oozeSource = findPermanent(player1, "Printlifter Ooze");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(oozeSource));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ooze");
    }

    private void castFaceDown() {
        harness.setHand(player1, java.util.List.of(new PrintlifterOoze()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
