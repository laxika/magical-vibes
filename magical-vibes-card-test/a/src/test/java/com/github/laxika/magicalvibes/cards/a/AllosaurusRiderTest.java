package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BalduvianFallen;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AllosaurusRider.class, SnowCoveredForest.class, SnowCoveredPlains.class,
        BalduvianFallen.class})
class AllosaurusRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness are one plus the number of lands its controller controls")
    void ptEqualsOnePlusControlledLands() {
        Permanent rider = addCreatureReady(player1, new AllosaurusRider());

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(1);

        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredPlains());
        harness.addToBattlefield(player2, new SnowCoveredForest());

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(3);

        harness.addToBattlefield(player1, new SnowCoveredForest());
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exiling two green cards from hand pays the alternative cost")
    void alternativeCostExilesTwoGreenCards() {
        harness.setHand(player1, List.of(new AllosaurusRider(), new AllosaurusRider(), new AllosaurusRider()));

        harness.castInstantWithAlternateExileFromHand(player1, 0, null, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactly("Allosaurus Rider", "Allosaurus Rider");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AllosaurusRider);
    }

    @Test
    @DisplayName("The normal mana cost remains available without exiling cards")
    void normalCostDoesNotExileCardsFromHand() {
        harness.castFromHand(player1, new AllosaurusRider(), "{5}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AllosaurusRider);
    }

    @Test
    @DisplayName("The alternative cost requires two green cards")
    void alternativeCostRejectsNonGreenCard() {
        harness.setHand(player1, List.of(new AllosaurusRider(), new SnowCoveredForest(), new BalduvianFallen()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, null, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void alternativeCostCannotExileTheSpellItself() {
        harness.setHand(player1, List.of(new AllosaurusRider(), new AllosaurusRider()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, null, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void alternativeCostRequiresTwoDistinctCards() {
        harness.setHand(player1, List.of(new AllosaurusRider(), new AllosaurusRider()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, null, List.of(1, 1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void alternativeCostCannotUseAForestEvenThoughItProducesGreenMana() {
        harness.setHand(player1, List.of(new AllosaurusRider(), new AllosaurusRider(),
                new SnowCoveredForest()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, null, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void alternativeCostIsPaidBeforeResolutionWithSpellInMiddleOfHand() {
        AllosaurusRider firstPayment = new AllosaurusRider();
        AllosaurusRider spell = new AllosaurusRider();
        AllosaurusRider secondPayment = new AllosaurusRider();
        BalduvianFallen remaining = new BalduvianFallen();
        harness.setHand(player1, List.of(firstPayment, spell, secondPayment, remaining));

        harness.castInstantWithAlternateExileFromHand(player1, 1, null, List.of(0, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(firstPayment, secondPayment);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == spell);
    }

    @Test
    void powerAndToughnessDecreaseWhenControlledLandsLeave() {
        Permanent rider = addCreatureReady(player1, new AllosaurusRider());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player2, new SnowCoveredForest());

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof SnowCoveredForest);

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(1);
    }
}
