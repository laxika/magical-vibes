package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NezumiLinkbreaker.class, Shock.class, GrizzlyBears.class})
class NezumiLinkbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("When Nezumi Linkbreaker dies, it creates a Mercenary token")
    void deathTriggerCreatesMercenaryToken() {
        Permanent linkbreaker = harness.addToBattlefieldAndReturn(player1, new NezumiLinkbreaker());

        destroyWithShock(linkbreaker);

        harness.assertInGraveyard(player1, "Nezumi Linkbreaker");
        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
    }

    @Test
    @DisplayName("The Mercenary token boosts a creature you control")
    void mercenaryBoostsCreatureYouControl() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent linkbreaker = harness.addToBattlefieldAndReturn(player1, new NezumiLinkbreaker());
        destroyWithShock(linkbreaker);

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Mercenary token cannot target an opposing creature")
    void mercenaryCannotTargetOpposingCreature() {
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent linkbreaker = harness.addToBattlefieldAndReturn(player1, new NezumiLinkbreaker());
        destroyWithShock(linkbreaker);

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, mercenaryIndex, 0, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("The Mercenary token can only be activated at sorcery speed")
    void mercenaryRequiresSorcerySpeed() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent linkbreaker = harness.addToBattlefieldAndReturn(player1, new NezumiLinkbreaker());
        destroyWithShock(linkbreaker);

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, mercenaryIndex, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("A newly created Mercenary cannot pay its tap cost")
    void mercenaryCannotActivateWhileSummoningSick() {
        Permanent linkbreaker = harness.addToBattlefieldAndReturn(player1, new NezumiLinkbreaker());
        destroyWithShock(linkbreaker);
        Permanent mercenary = findPermanent(player1, "Mercenary");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(mercenary.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Mercenary can boost itself and the boost expires at cleanup")
    void mercenaryCanBoostItselfUntilEndOfTurn() {
        Permanent linkbreaker = harness.addToBattlefieldAndReturn(player1, new NezumiLinkbreaker());
        destroyWithShock(linkbreaker);
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        harness.activateAbility(player1, index, 0, null, mercenary.getId());
        harness.passBothPriorities();

        assertThat(mercenary.getPowerModifier()).isEqualTo(1);
        assertThat(mercenary.getToughnessModifier()).isZero();
        assertThat(mercenary.isTapped()).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(mercenary.getPowerModifier()).isZero();
        assertThat(mercenary.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A Mercenary cannot activate in response to a spell during its controller's main phase")
    void mercenaryRequiresEmptyStack() {
        Permanent linkbreaker = harness.addToBattlefieldAndReturn(player1, new NezumiLinkbreaker());
        destroyWithShock(linkbreaker);
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(mercenary.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    private void destroyWithShock(Permanent linkbreaker) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, linkbreaker.getId());
        harness.passBothPriorities();
    }
}
