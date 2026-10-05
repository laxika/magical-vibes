package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MoorFiend;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PestilenceRats.class, MoorFiend.class})
class PestilenceRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Pestilence Rats is 0/3 with no other Rats")
    void zeroPowerWithNoOtherRats() {
        Permanent rats = addCreatureReady(player1, new PestilenceRats());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pestilence Rats power equals other Rats you control")
    void powerEqualsOtherControlledRats() {
        Permanent rats = addCreatureReady(player1, new PestilenceRats());
        harness.addToBattlefield(player1, new PestilenceRats());
        harness.addToBattlefield(player1, new PestilenceRats());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pestilence Rats counts opponent Rats")
    void countsOpponentRats() {
        Permanent rats = addCreatureReady(player1, new PestilenceRats());
        harness.addToBattlefield(player2, new PestilenceRats());
        harness.addToBattlefield(player2, new PestilenceRats());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pestilence Rats does not count non-Rat creatures")
    void doesNotCountNonRats() {
        Permanent rats = addCreatureReady(player1, new PestilenceRats());
        harness.addToBattlefield(player1, new MoorFiend());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pestilence Rats power updates when other Rats leave")
    void powerUpdatesWhenRatsLeave() {
        Permanent rats = addCreatureReady(player1, new PestilenceRats());
        Permanent controlledRat = harness.addToBattlefieldAndReturn(player1, new PestilenceRats());
        Permanent opponentRat = harness.addToBattlefieldAndReturn(player2, new PestilenceRats());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(controlledRat);

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(1);

        gd.playerBattlefields.get(player2.getId()).remove(opponentRat);

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pestilence Rats counts only Rats on the battlefield")
    void doesNotCountRatsInOtherZones() {
        Permanent rats = addCreatureReady(player1, new PestilenceRats());
        harness.addToBattlefield(player2, new PestilenceRats());
        harness.setHand(player1, List.of(new PestilenceRats()));
        harness.setLibrary(player1, List.of(new PestilenceRats()));
        harness.setGraveyard(player2, List.of(new PestilenceRats()));
        harness.setExile(player2, List.of(new PestilenceRats()));

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pestilence Rats defines its power outside the battlefield")
    void powerIsDefinedInHandAndGraveyard() {
        PestilenceRats inHand = new PestilenceRats();
        PestilenceRats inGraveyard = new PestilenceRats();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player2, List.of(inGraveyard));
        harness.addToBattlefield(player1, new PestilenceRats());
        harness.addToBattlefield(player2, new PestilenceRats());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(3);
    }
}
