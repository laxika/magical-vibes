package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({KraumViolentCacophony.class, Shock.class})
class KraumViolentCacophonyTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell each turn puts a counter on Kraum and draws a card")
    void secondSpellPutsCounterAndDrawsCard() {
        Permanent kraum = addCreatureReady(player1, new KraumViolentCacophony());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(kraum.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(kraum.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(kraum.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting Kraum as the first spell counts toward the second spell trigger")
    void kraumItselfCountsAsFirstSpell() {
        harness.setHand(player1, List.of(new KraumViolentCacophony(), new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent kraum = findPermanent(player1, "Kraum, Violent Cacophony");
        assertThat(kraum.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(kraum.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(kraum.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Kraum cast as the second spell does not trigger for itself or the third spell")
    void enteringAsSecondSpellDoesNotTriggerRetroactively() {
        harness.setHand(player1, List.of(new Shock(), new KraumViolentCacophony(), new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent kraum = findPermanent(player1, "Kraum, Violent Cacophony");
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(kraum.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's spells neither trigger Kraum nor count toward its controller's second spell")
    void opponentsSpellsDoNotCount() {
        Permanent kraum = addCreatureReady(player1, new KraumViolentCacophony());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(kraum.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(kraum.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The card draw still happens when Kraum dies before its ability resolves")
    void drawsEvenWhenSourceDiesInResponse() {
        Permanent kraum = addCreatureReady(player1, new KraumViolentCacophony());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player2, 0, kraum.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, kraum.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kraum);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Kraum can trigger again on an opponent's turn after the spell count resets")
    void triggersAgainOnOpponentsTurn() {
        Permanent kraum = addCreatureReady(player1, new KraumViolentCacophony());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(kraum.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(kraum.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(kraum.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
