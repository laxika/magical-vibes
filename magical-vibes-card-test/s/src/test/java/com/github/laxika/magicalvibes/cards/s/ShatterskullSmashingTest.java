package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CanopyBaloth;
import com.github.laxika.magicalvibes.cards.n.NissaOfShadowedBoughs;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatterskullSmashing.class, ShatterskullTheHammerPass.class, GrizzlyBears.class,
        CanopyBaloth.class, NissaOfShadowedBoughs.class})
class ShatterskullSmashingTest extends BaseCardTest {

    @Test
    void dealsXDamageBelowTheThreshold() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShatterskullSmashing()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorceryWithModesForXAndDamageAssignments(
                player1, 0, 1, new int[]{0}, 4, Map.of(bears.getId(), 4));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void dealsTwiceXDamageAtTheThresholdAndDividesItAmongTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShatterskullSmashing()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castModalSorceryWithModesForXAndDamageAssignments(
                player1, 0, 1, new int[]{0}, 6,
                Map.of(first.getId(), 6, second.getId(), 6));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(first.getCard(), second.getCard());
    }

    @Test
    void rejectsMoreThanTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShatterskullSmashing()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForXAndDamageAssignments(
                player1, 0, 1, new int[]{0}, 6,
                Map.of(first.getId(), 4, second.getId(), 4, third.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landFaceMayPayThreeLifeAndProducesRedMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ShatterskullSmashing()));

        gs.playCard(gd, player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(ShatterskullTheHammerPass.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(land.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void doubledDamageCanBeDividedIntoOddAmountsAmongCreatureAndPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CanopyBaloth());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NissaOfShadowedBoughs());
        harness.setHand(player1, List.of(new ShatterskullSmashing()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castModalSorceryWithModesForXAndDamageAssignments(
                player1, 0, 1, new int[]{0}, 6,
                Map.of(creature.getId(), 11, planeswalker.getId(), 1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Canopy Baloth");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void remainingTargetReceivesOnlyItsOriginalShare() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CanopyBaloth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CanopyBaloth());
        harness.setHand(player1, List.of(new ShatterskullSmashing()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorceryWithModesForXAndDamageAssignments(
                player1, 0, 1, new int[]{0}, 4,
                Map.of(first.getId(), 2, second.getId(), 2));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerHands.get(player2.getId()).add(first.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second);
        assertThat(second.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void canCastWithZeroXAndNoTargets() {
        harness.setHand(player1, List.of(new ShatterskullSmashing()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorceryWithModesForXAndDamageAssignments(
                player1, 0, 1, new int[]{0}, 0, Map.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shatterskull Smashing");
    }

    @Test
    void canChooseNoTargetsWithPositiveX() {
        harness.setHand(player1, List.of(new ShatterskullSmashing()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorceryWithModesForXAndDamageAssignments(
                player1, 0, 1, new int[]{0}, 4, Map.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shatterskull Smashing");
    }

    @Test
    void rejectsZeroDamageAssignedToATarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CanopyBaloth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CanopyBaloth());
        harness.setHand(player1, List.of(new ShatterskullSmashing()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForXAndDamageAssignments(
                player1, 0, 1, new int[]{0}, 4,
                Map.of(first.getId(), 4, second.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsPlayerAsTarget() {
        harness.setHand(player1, List.of(new ShatterskullSmashing()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForXAndDamageAssignments(
                player1, 0, 1, new int[]{0}, 4, Map.of(player2.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landFaceEntersTappedWhenLifePaymentIsDeclined() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ShatterskullSmashing()));

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.assertLife(player1, 20);
    }

    @Test
    void landFaceEntersTappedWhenThreeLifeCannotBePaid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 2);
        harness.setHand(player1, List.of(new ShatterskullSmashing()));

        gs.playCard(gd, player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.assertLife(player1, 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void dealsExactlyTwiceXAboveTheThresholdToASinglePlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NissaOfShadowedBoughs());
        planeswalker.setCounterCount(CounterType.LOYALTY, 20);
        harness.setHand(player1, List.of(new ShatterskullSmashing()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castModalSorceryWithModesForXAndDamageAssignments(
                player1, 0, 1, new int[]{0}, 7, Map.of(planeswalker.getId(), 14));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void dealsExactlyXImmediatelyBelowTheThreshold() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NissaOfShadowedBoughs());
        planeswalker.setCounterCount(CounterType.LOYALTY, 10);
        harness.setHand(player1, List.of(new ShatterskullSmashing()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castModalSorceryWithModesForXAndDamageAssignments(
                player1, 0, 1, new int[]{0}, 5, Map.of(planeswalker.getId(), 5));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void rejectsUndoubledTotalAtTheThreshold() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CanopyBaloth());
        harness.setHand(player1, List.of(new ShatterskullSmashing()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForXAndDamageAssignments(
                player1, 0, 1, new int[]{0}, 6, Map.of(creature.getId(), 6)))
                .isInstanceOf(IllegalStateException.class);
    }
}
