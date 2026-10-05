package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WickerWitch;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KessigDireSwine.class, Forest.class, GrizzlyBears.class, Millstone.class, Shock.class, WickerWitch.class})
class KessigDireSwineTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have trample without delirium")
    void noDelirium() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        Permanent swine = harness.addToBattlefieldAndReturn(player1, new KessigDireSwine());

        assertThat(gqs.hasKeyword(gd, swine, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Has trample with four card types in its controller's graveyard")
    void delirium() {
        setDelirium();
        Permanent swine = harness.addToBattlefieldAndReturn(player1, new KessigDireSwine());

        assertThat(gqs.hasKeyword(gd, swine, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's graveyard does not count toward delirium")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        Permanent swine = harness.addToBattlefieldAndReturn(player1, new KessigDireSwine());

        assertThat(gqs.hasKeyword(gd, swine, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Loses trample when its graveyard drops below four card types")
    void losesTrampleWhenDeliriumIsLost() {
        setDelirium();
        Permanent swine = harness.addToBattlefieldAndReturn(player1, new KessigDireSwine());
        assertThat(gqs.hasKeyword(gd, swine, Keyword.TRAMPLE)).isTrue();

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        assertThat(gqs.hasKeyword(gd, swine, Keyword.TRAMPLE)).isFalse();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }

    @Test
    @DisplayName("Gains trample immediately when delirium becomes active")
    void gainsTrampleWhenDeliriumIsGained() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        Permanent swine = harness.addToBattlefieldAndReturn(player1, new KessigDireSwine());
        assertThat(gqs.hasKeyword(gd, swine, Keyword.TRAMPLE)).isFalse();

        setDelirium();

        assertThat(gqs.hasKeyword(gd, swine, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Four cards with only three distinct types do not enable delirium")
    void duplicateTypesDoNotCountTwice() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Forest(), new Shock()));
        Permanent swine = harness.addToBattlefieldAndReturn(player1, new KessigDireSwine());

        assertThat(gqs.hasKeyword(gd, swine, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An artifact creature contributes both card types to delirium")
    void multipleTypesOnOneCardCountSeparately() {
        harness.setGraveyard(player1, List.of(new WickerWitch(), new Forest(), new Shock()));
        Permanent swine = harness.addToBattlefieldAndReturn(player1, new KessigDireSwine());

        assertThat(gqs.hasKeyword(gd, swine, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Delirium grants trample only to Kessig Dire Swine")
    void doesNotGrantTrampleToOtherCreatures() {
        setDelirium();
        Permanent swine = harness.addToBattlefieldAndReturn(player1, new KessigDireSwine());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, swine, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }
}
