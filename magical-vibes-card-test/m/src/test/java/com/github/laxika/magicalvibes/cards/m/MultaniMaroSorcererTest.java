package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.s.Swat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MultaniMaroSorcerer.class, GiantCockroach.class, Swat.class})
class MultaniMaroSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the total number of cards in all players' hands")
    void ptEqualsTotalHandSize() {
        Permanent multani = addCreatureReady(player1, new MultaniMaroSorcerer());
        harness.setHand(player1, List.of(new GiantCockroach()));
        harness.setHand(player2, List.of(new GiantCockroach(), new GiantCockroach()));

        assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, multani)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power and toughness update when any player's hand changes")
    void ptUpdatesWhenHandsChange() {
        Permanent multani = addCreatureReady(player1, new MultaniMaroSorcerer());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new GiantCockroach()));
        assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(1);

        harness.setHand(player2, List.of(new GiantCockroach()));
        assertThat(gqs.getEffectiveToughness(gd, multani)).isEqualTo(2);

        harness.setHand(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot be targeted by spells because it has shroud")
    void cannotBeTargetedBySpells() {
        Permanent multani = addCreatureReady(player1, new MultaniMaroSorcerer());
        harness.setHand(player1, List.of(new Swat()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, multani.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud also prevents an opponent from targeting Multani")
    void opponentCannotTargetMultani() {
        harness.setHand(player1, List.of(new GiantCockroach()));
        harness.setHand(player2, List.of(new Swat()));
        Permanent multani = addCreatureReady(player1, new MultaniMaroSorcerer());
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, multani.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Casting Multani removes it from the hand count")
    void castingUsesRemainingCardsInHands() {
        harness.setHand(player1, List.of(new MultaniMaroSorcerer(), new GiantCockroach()));
        harness.setHand(player2, List.of(new GiantCockroach(), new GiantCockroach()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Multani, Maro-Sorcerer");
        Permanent multani = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, multani)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, multani)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multani dies on resolution when all hands are empty")
    void diesWhenCastingLeavesAllHandsEmpty() {
        harness.setHand(player1, List.of(new MultaniMaroSorcerer()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Multani, Maro-Sorcerer");
        harness.assertInGraveyard(player1, "Multani, Maro-Sorcerer");
    }

    @Test
    @DisplayName("Multani's characteristic power and toughness update in the graveyard")
    void characteristicPowerToughnessWorksInGraveyard() {
        MultaniMaroSorcerer multani = new MultaniMaroSorcerer();
        harness.setGraveyard(player1, List.of(multani));
        harness.setHand(player1, List.of(new GiantCockroach()));
        harness.setHand(player2, List.of(new GiantCockroach(), new GiantCockroach()));

        assertThat(gqs.getEffectiveCardPower(gd, multani)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, multani)).isEqualTo(3);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        assertThat(gqs.getEffectiveCardPower(gd, multani)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, multani)).isZero();
    }
}
