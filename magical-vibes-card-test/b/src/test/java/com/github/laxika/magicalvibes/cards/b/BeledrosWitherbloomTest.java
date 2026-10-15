package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeledrosWitherbloom.class, Forest.class, GrizzlyBears.class, Shock.class})
class BeledrosWitherbloomTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Pest during each upkeep, and its death gains 1 life")
    void createsPestDuringEachUpkeepAndPestDeathGainsLife() {
        harness.addToBattlefield(player1, new BeledrosWitherbloom());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        Permanent pest = findPermanent(player1, "Pest");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, pest.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Pays 10 life and untaps only lands controlled by Beledros's controller")
    void paysLifeAndUntapsControlledLands() {
        harness.addToBattlefield(player1, new BeledrosWitherbloom());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        bears.tap();
        opposingForest.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(forest.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
        assertThat(opposingForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap ability can only be activated once each turn")
    void untapAbilityOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new BeledrosWitherbloom());
        harness.setLife(player1, 30);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Life is paid immediately, but lands untap only when the ability resolves")
    void paysLifeBeforeUntappingLands() {
        Permanent beledros = harness.addToBattlefieldAndReturn(player1, new BeledrosWitherbloom());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        beledros.tap();
        forest.tap();
        harness.setLife(player1, 30);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        resolveAllTriggers();

        assertThat(forest.isTapped()).isFalse();
        assertThat(beledros.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Insufficient life prevents activation without spending life or untapping lands")
    void cannotActivateWithLessThanTenLife() {
        harness.addToBattlefield(player1, new BeledrosWitherbloom());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.setLife(player1, 9);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(9);
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();

        harness.setLife(player1, 11);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The activation limit resets on the opponent's turn")
    void canActivateAgainOnOpponentsTurn() {
        harness.addToBattlefield(player1, new BeledrosWitherbloom());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLife(player1, 30);
        forest.tap();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.UPKEEP);
        resolveAllTriggers();
        forest.tap();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(forest.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("A Pest created on another player's upkeep belongs to Beledros's controller")
    void opposingControllerCreatesPestAndGainsLifeWhenItDies() {
        harness.addToBattlefield(player2, new BeledrosWitherbloom());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        Permanent pest = findPermanent(player2, "Pest");
        assertThat(countPermanents(player2, "Pest")).isEqualTo(1);
        assertThat(countPermanents(player1, "Pest")).isZero();
        assertThat(pest.getCard().isToken()).isTrue();
        assertThat(pest.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(pest.getCard().getSubtypes()).containsExactly(CardSubtype.PEST);
        assertThat(gqs.getEffectivePower(gd, pest)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pest)).isEqualTo(1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, pest.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Pest")).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
    }
}
