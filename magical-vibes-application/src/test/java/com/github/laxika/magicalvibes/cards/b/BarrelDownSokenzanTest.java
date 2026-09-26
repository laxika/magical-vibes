package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArashiTheSkyAsunder;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({BarrelDownSokenzan.class, ArashiTheSkyAsunder.class, Mountain.class})
class BarrelDownSokenzanTest extends BaseCardTest {

    @Test
    @DisplayName("Returns chosen Mountains and deals twice their number to the target creature")
    void returnsChosenMountainsAndDealsTwiceTheirNumber() {
        Permanent target = addCreatureReady(player2, new ArashiTheSkyAsunder());
        Permanent firstMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent secondMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent thirdMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent nonMountain = addCreatureReady(player1, new ArashiTheSkyAsunder());
        Permanent opposingMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        castBarrelDownSokenzan(target);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(
                firstMountain.getId(), secondMountain.getId(), thirdMountain.getId());
        assertThat(choice.maxCount()).isEqualTo(3);

        harness.handleMultiplePermanentsChosen(player1,
                List.of(firstMountain.getId(), secondMountain.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(thirdMountain, nonMountain)
                .doesNotContain(firstMountain, secondMountain);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target, opposingMountain);
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstMountain.getCard(), secondMountain.getCard());
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Choosing no Mountains is legal and deals no damage")
    void choosingNoMountainsDealsNoDamage() {
        Permanent target = addCreatureReady(player2, new ArashiTheSkyAsunder());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        castBarrelDownSokenzan(target);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mountain);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With no controlled Mountains, resolves without a choice and deals no damage")
    void resolvesWithoutControlledMountains() {
        Permanent target = addCreatureReady(player2, new ArashiTheSkyAsunder());
        Permanent opponentMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        castBarrelDownSokenzan(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target, opponentMountain);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Returns only controlled Mountains to their owners' hands")
    void returnsOnlyControlledMountainsToTheirOwnersHands() {
        Permanent target = addCreatureReady(player2, new ArashiTheSkyAsunder());
        Mountain controlledCardOwnedByOpponent = new Mountain();
        controlledCardOwnedByOpponent.setOwnerId(player2.getId());
        Permanent controlledMountain = harness.addToBattlefieldAndReturn(player1, controlledCardOwnedByOpponent);
        Permanent opponentMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player2, List.of());

        castBarrelDownSokenzan(target);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(controlledMountain.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(controlledMountain.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(controlledMountain);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target, opponentMountain);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(controlledCardOwnedByOpponent);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(controlledCardOwnedByOpponent);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new BarrelDownSokenzan()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature");
    }

    private void castBarrelDownSokenzan(Permanent target) {
        harness.setHand(player1, List.of(new BarrelDownSokenzan()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
    }
}
