package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BallynockCohort;
import com.github.laxika.magicalvibes.cards.e.ElsewhereFlask;
import com.github.laxika.magicalvibes.cards.w.WoodfallPrimus;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrderOfWhiteclay.class, BallynockCohort.class, ElsewhereFlask.class, WoodfallPrimus.class})
class OrderOfWhiteclayTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{W}{W} and untapping reanimates a creature with mana value 3")
    void reanimatesCreatureWithManaValueThreeAndUntapsSource() {
        Permanent order = addTapped(player1, new OrderOfWhiteclay());
        harness.addMana(player1, ManaColor.WHITE, 3);

        Card target = new BallynockCohort();
        harness.setGraveyard(player1, List.of(target));

        enterMainWithPriority(player1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ballynock Cohort");
        harness.assertNotInGraveyard(player1, "Ballynock Cohort");
        // Paying {Q} untapped the source.
        assertThat(order.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addCreatureReady(player1, new OrderOfWhiteclay());
        harness.addMana(player1, ManaColor.WHITE, 3);

        Card target = new BallynockCohort();
        harness.setGraveyard(player1, List.of(target));

        enterMainWithPriority(player1);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    @Test
    @DisplayName("Cannot target a creature card with mana value greater than 3")
    void cannotTargetHighManaValueCreature() {
        addTapped(player1, new OrderOfWhiteclay());
        harness.addMana(player1, ManaColor.WHITE, 3);

        Card target = new WoodfallPrimus();
        harness.setGraveyard(player1, List.of(target));

        enterMainWithPriority(player1);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature card even when its mana value is 3 or less")
    void cannotTargetNoncreatureCard() {
        addTapped(player1, new OrderOfWhiteclay());
        harness.addMana(player1, ManaColor.WHITE, 3);

        Card target = new ElsewhereFlask();
        harness.setGraveyard(player1, List.of(target));

        enterMainWithPriority(player1);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        addTapped(player1, new OrderOfWhiteclay());
        harness.addMana(player1, ManaColor.WHITE, 3);

        Card target = new BallynockCohort();
        harness.setGraveyard(player2, List.of(target));

        enterMainWithPriority(player1);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotPayUntapCostWhileSummoningSick() {
        Permanent order = addTapped(player1, new OrderOfWhiteclay());
        order.setSummoningSick(true);
        Card target = new BallynockCohort();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 3);
        enterMainWithPriority(player1);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(order.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void paysManaAndUntapsBeforeResolutionEvenIfTargetLeavesGraveyard() {
        Permanent order = addTapped(player1, new OrderOfWhiteclay());
        Card target = new BallynockCohort();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        enterMainWithPriority(player1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));

        assertThat(order.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertInGraveyard(player1, "Ballynock Cohort");
        harness.assertNotOnBattlefield(player1, "Ballynock Cohort");

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ballynock Cohort");
        assertThat(order.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent order = addTapped(player1, new OrderOfWhiteclay());
        Card target = new BallynockCohort();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 3);
        enterMainWithPriority(player1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(order);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ballynock Cohort");
        harness.assertNotInGraveyard(player1, "Ballynock Cohort");
    }

    @Test
    void cannotActivateWithoutEnoughWhiteMana() {
        Permanent order = addTapped(player1, new OrderOfWhiteclay());
        Card target = new BallynockCohort();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        enterMainWithPriority(player1);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(order.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ballynock Cohort");
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
