package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElementalBond;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrintlifterOoze.class, SakuraTribeElder.class, ElementalBond.class})
class PrintlifterOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Turning face up counts the source and all other controlled creatures")
    void turnsFaceUpCreatesScaledOoze() {
        addCreatureReady(player1, new SakuraTribeElder());
        addCreatureReady(player1, new SakuraTribeElder());
        addCreatureReady(player2, new SakuraTribeElder());

        castFaceDown();
        Permanent oozeSource = findPermanent(player1, "Printlifter Ooze");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(oozeSource));
        harness.passBothPriorities();

        Permanent ooze = findPermanent(player1, "Ooze");
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(ooze.getEffectivePower()).isEqualTo(3);
        assertThat(ooze.getEffectiveToughness()).isEqualTo(3);
        assertThat(ooze.getCard().getKeywords()).contains(Keyword.TRAMPLE);
        assertThat(ooze.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    @DisplayName("With only Printlifter Ooze controlled, its token survives with one counter")
    void sourceAloneCreatesOneCounterOoze() {
        castFaceDown();
        Permanent oozeSource = findPermanent(player1, "Printlifter Ooze");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(oozeSource));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ooze").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void anotherControlledCreatureTurningFaceUpTriggersEachFaceUpOoze() {
        addCreatureReady(player1, new PrintlifterOoze());
        castFaceDown();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, 1);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ooze"))
                .extracting(p -> p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .containsExactlyInAnyOrder(2, 3);
    }

    @Test
    void opponentTurningFaceUpDoesNotTriggerOurOoze() {
        addCreatureReady(player1, new PrintlifterOoze());
        harness.forceActivePlayer(player2);
        castFaceDown(player2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ooze");
        assertThat(findPermanent(player2, "Ooze").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void tokenEntersWithCountersForPowerBasedEntryTriggers() {
        harness.addToBattlefield(player1, new ElementalBond());
        addCreatureReady(player1, new SakuraTribeElder());
        addCreatureReady(player1, new SakuraTribeElder());
        addCreatureReady(player1, new SakuraTribeElder());
        harness.setLibrary(player1, java.util.List.of(new SakuraTribeElder()));
        castFaceDown();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, 4);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void castFaceDown() {
        castFaceDown(player1);
    }

    private void castFaceDown(Player player) {
        harness.setHand(player, java.util.List.of(new PrintlifterOoze()));
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
