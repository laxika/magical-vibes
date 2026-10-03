package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HaazdaExonerator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonsJester.class, HaazdaExonerator.class})
class DemonsJesterTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+1 with an empty hand")
    void getsHellbentBonusWithEmptyHand() {
        harness.setHand(player1, List.of());
        Permanent jester = harness.addToBattlefieldAndReturn(player1, new DemonsJester());

        assertThat(gqs.getEffectivePower(gd, jester)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jester)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses the bonus while its controller has a card in hand")
    void losesHellbentBonusWithCardInHand() {
        harness.setHand(player1, List.of(new HaazdaExonerator()));
        Permanent jester = harness.addToBattlefieldAndReturn(player1, new DemonsJester());

        assertThat(gqs.getEffectivePower(gd, jester)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jester)).isEqualTo(2);
    }

    @Test
    @DisplayName("The bonus changes dynamically when the hand changes")
    void bonusChangesWhenHandChanges() {
        harness.setHand(player1, List.of());
        Permanent jester = harness.addToBattlefieldAndReturn(player1, new DemonsJester());

        assertThat(gqs.getEffectivePower(gd, jester)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jester)).isEqualTo(3);

        harness.setHand(player1, List.of(new HaazdaExonerator()));
        assertThat(gqs.getEffectivePower(gd, jester)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jester)).isEqualTo(2);
    }

    @Test
    @DisplayName("Checks only its controller's hand")
    void ignoresOpponentsHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new HaazdaExonerator()));

        Permanent jester = harness.addToBattlefieldAndReturn(player1, new DemonsJester());

        assertThat(gqs.getEffectivePower(gd, jester)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jester)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gains the bonus immediately when the last card is cast")
    void gainsBonusWhenLastCardIsCast() {
        harness.setHand(player1, List.of(new HaazdaExonerator()));
        Permanent jester = harness.addToBattlefieldAndReturn(player1, new DemonsJester());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThat(gqs.getEffectivePower(gd, jester)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jester)).isEqualTo(2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, jester)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jester)).isEqualTo(3);
    }

    @Test
    @DisplayName("Hellbent boosts only this creature")
    void doesNotBoostOtherCreatures() {
        harness.setHand(player1, List.of());
        Permanent jester = harness.addToBattlefieldAndReturn(player1, new DemonsJester());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new HaazdaExonerator());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HaazdaExonerator());

        assertThat(gqs.getEffectivePower(gd, jester)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jester)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(1);
    }
}
