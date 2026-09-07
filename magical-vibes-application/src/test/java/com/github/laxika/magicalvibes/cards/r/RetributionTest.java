package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AnabaBodyguard;
import com.github.laxika.magicalvibes.cards.w.WillowFaerie;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Retribution.class, AnabaBodyguard.class, WillowFaerie.class})
class RetributionTest extends BaseCardTest {

    private void castRetribution(Permanent first, Permanent second) {
        harness.setHand(player1, List.of(new Retribution()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The targeted creatures' controller chooses which one to sacrifice")
    void opponentChoosesWhichCreatureToSacrifice() {
        Permanent bodyguard = addCreatureReady(player2, new AnabaBodyguard());
        Permanent faerie = addCreatureReady(player2, new WillowFaerie());

        castRetribution(bodyguard, faerie);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bodyguard.getId(), faerie.getId());
    }

    @Test
    @DisplayName("The chosen creature is sacrificed and the other gets a -1/-1 counter")
    void sacrificesChosenAndPutsCounterOnOther() {
        Permanent bodyguard = addCreatureReady(player2, new AnabaBodyguard());
        Permanent faerie = addCreatureReady(player2, new WillowFaerie());

        castRetribution(bodyguard, faerie);
        harness.handlePermanentChosen(player2, bodyguard.getId());

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Anaba Bodyguard"));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(faerie);
        assertThat(faerie.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(faerie.getEffectivePower()).isEqualTo(0);
        assertThat(faerie.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing the second target sacrifices it and counters the first")
    void choosingSecondTargetSacrificesItAndCountersFirst() {
        Permanent bodyguard = addCreatureReady(player2, new AnabaBodyguard());
        Permanent faerie = addCreatureReady(player2, new WillowFaerie());

        castRetribution(bodyguard, faerie);
        harness.handlePermanentChosen(player2, faerie.getId());

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Willow Faerie"));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bodyguard);
        assertThat(bodyguard.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(bodyguard.getEffectivePower()).isEqualTo(1);
        assertThat(bodyguard.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The -1/-1 counter can be lethal to the surviving creature")
    void counterCanKillTheSurvivor() {
        Permanent bodyguard = addCreatureReady(player2, new AnabaBodyguard());
        Permanent shrunk = addCreatureReady(player2, new WillowFaerie());
        shrunk.setToughnessModifier(-1);

        castRetribution(bodyguard, shrunk);
        harness.handlePermanentChosen(player2, bodyguard.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("With only one target left legal, that one is sacrificed and no counter is placed")
    void singleRemainingTargetIsSacrificedWithoutCounter() {
        Permanent bodyguard = addCreatureReady(player2, new AnabaBodyguard());
        Permanent faerie = addCreatureReady(player2, new WillowFaerie());

        harness.setHand(player1, List.of(new Retribution()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of(bodyguard.getId(), faerie.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(faerie);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Anaba Bodyguard"));
    }

    @Test
    @DisplayName("Creatures you control are not legal targets")
    void cannotTargetYourOwnCreatures() {
        Permanent own = addCreatureReady(player1, new AnabaBodyguard());
        Permanent theirs = addCreatureReady(player2, new WillowFaerie());

        harness.setHand(player1, List.of(new Retribution()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(own.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires two distinct target creatures")
    void requiresTwoDistinctTargets() {
        Permanent bodyguard = addCreatureReady(player2, new AnabaBodyguard());

        harness.setHand(player1, List.of(new Retribution()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(bodyguard.getId(), bodyguard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }
}
