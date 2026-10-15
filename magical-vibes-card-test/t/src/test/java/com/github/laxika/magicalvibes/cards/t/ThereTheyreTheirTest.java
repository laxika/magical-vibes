package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThereTheyreTheir.class, ForestBear.class})
class ThereTheyreTheirTest extends BaseCardTest {

    @Test
    void thereFlickersACreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        harness.setHand(player1, List.of(new ThereTheyreTheir()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest Bear");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void theyreMakesUpToThreeCreaturesUnblockable() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        harness.setHand(player1, List.of(new ThereTheyreTheir()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 1,
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(first.isCantBeBlocked()).isTrue();
        assertThat(second.isCantBeBlocked()).isTrue();
        assertThat(third.isCantBeBlocked()).isTrue();
        assertThat(fourth.isCantBeBlocked()).isFalse();
    }

    @Test
    void theirGivesAnOpponentControlOfYourCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        harness.setHand(player1, List.of(new ThereTheyreTheir()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 2, List.of(player2.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    void thereCannotTargetACreatureAnOpponentControls() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        harness.setHand(player1, List.of(new ThereTheyreTheir()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void theyreCanBeCastWithoutTargets() {
        harness.setHand(player1, List.of(new ThereTheyreTheir()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "There // They're // Their");
    }

    @Test
    void theyreCannotTargetFourCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        harness.setHand(player1, List.of(new ThereTheyreTheir()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void thereReturnsAGivenAwayCreatureToItsOwnerAsANewPermanent() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        harness.setHand(player2, List.of(new ThereTheyreTheir()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castModalInstant(player2, 0, 2, List.of(player1.getId(), bear.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Forest Bear");

        harness.setHand(player1, List.of(new ThereTheyreTheir()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castModalInstant(player1, 0, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest Bear");
        harness.assertOnBattlefield(player2, "Forest Bear");
        assertThat(harness.getPermanentId(player2, "Forest Bear")).isNotEqualTo(bear.getId());
    }

    @Test
    void theyreStillAffectsTheRemainingTargetAfterAnotherIsFlickered() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        harness.setHand(player1, List.of(new ThereTheyreTheir()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstant(player1, 0, 1, List.of(first.getId(), second.getId()));

        harness.setHand(player2, List.of(new ThereTheyreTheir()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castModalInstant(player2, 0, 0, List.of(second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.isCantBeBlocked()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement().satisfies(returned -> {
                    assertThat(returned.getId()).isNotEqualTo(second.getId());
                    assertThat(returned.isCantBeBlocked()).isFalse();
                });
    }

    @Test
    void theirCannotGiveYourCreatureToYourself() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        harness.setHand(player1, List.of(new ThereTheyreTheir()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 2,
                List.of(player1.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void theirCannotGiveAwayAnOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        harness.setHand(player1, List.of(new ThereTheyreTheir()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 2,
                List.of(player2.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void theyreCanTargetASingleCreatureAndExpiresAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        harness.setHand(player1, List.of(new ThereTheyreTheir()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 1, List.of(bear.getId()));
        harness.passBothPriorities();
        assertThat(bear.isCantBeBlocked()).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(bear.isCantBeBlocked()).isFalse();
    }

    @Test
    void theyreCanTargetExactlyTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        harness.setHand(player1, List.of(new ThereTheyreTheir()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isCantBeBlocked()).isTrue();
        assertThat(second.isCantBeBlocked()).isTrue();
    }

    @Test
    void theirControlChangeDoesNotExpireAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        harness.setHand(player1, List.of(new ThereTheyreTheir()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 2, List.of(player2.getId(), bear.getId()));
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        harness.assertNotOnBattlefield(player1, "Forest Bear");
        assertThat(harness.getPermanentId(player2, "Forest Bear")).isEqualTo(bear.getId());
    }
}
