package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
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

@CardUsed({DismissivePyromancer.class, GreenwoodSentinel.class, Forest.class, ColossalDreadmaw.class})
class DismissivePyromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card and draws a card")
    void discardsAndDraws() {
        addCreatureReady(player1, new DismissivePyromancer());
        harness.setHand(player1, List.of(new GreenwoodSentinel()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Sacrifices itself and deals 4 damage to target creature")
    void sacrificesAndDealsDamageToCreature() {
        addCreatureReady(player1, new DismissivePyromancer());
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent target = findPermanent(player2, "Greenwood Sentinel");
        harness.activateAbility(player1, 0, 1, null, target.getId());

        harness.assertInGraveyard(player1, "Dismissive Pyromancer");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Discard and tap are paid before drawing on resolution")
    void paysDiscardAndTapBeforeDrawing() {
        Permanent pyromancer = addCreatureReady(player1, new DismissivePyromancer());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(pyromancer.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Greenwood Sentinel");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate the draw ability with an empty hand")
    void cannotDrawWithoutDiscarding() {
        Permanent pyromancer = addCreatureReady(player1, new DismissivePyromancer());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pyromancer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both abilities are unavailable while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new DismissivePyromancer());
        Permanent pyromancer = findPermanent(player1, "Dismissive Pyromancer");
        pyromancer.setSummoningSick(true);
        harness.setHand(player1, List.of(new Forest()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pyromancer.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Dismissive Pyromancer");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both abilities are unavailable while tapped")
    void cannotActivateWhileTapped() {
        Permanent pyromancer = addCreatureReady(player1, new DismissivePyromancer());
        pyromancer.tap();
        harness.setHand(player1, List.of(new Forest()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Dismissive Pyromancer");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deals exactly four damage to a creature its controller owns")
    void dealsFourDamageToOwnCreature() {
        addCreatureReady(player1, new DismissivePyromancer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        harness.assertInGraveyard(player1, "Dismissive Pyromancer");
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        addCreatureReady(player1, new DismissivePyromancer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Dismissive Pyromancer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself but is sacrificed before the ability resolves")
    void canTargetItself() {
        Permanent pyromancer = addCreatureReady(player1, new DismissivePyromancer());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 1, null, pyromancer.getId());

        harness.assertInGraveyard(player1, "Dismissive Pyromancer");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
