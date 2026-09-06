package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfernalHarvest.class, GrizzlyBears.class, Plains.class, Swamp.class})
class InfernalHarvestTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new InfernalHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Returns Swamps and divides that much damage among target creatures")
    void returnsSwampsAndDividesDamage() {
        Permanent swamp1 = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent swamp2 = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        harness.castSorceryReturningPermanents(player1, 0,
                Map.of(first.getId(), 2),
                List.of(swamp1.getId(), swamp2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).extracting(c -> c.getName())
                .contains("Swamp", "Swamp");
        assertThat(second.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can divide damage among multiple target creatures")
    void dividesDamageAmongMultipleTargetCreatures() {
        Permanent swamp1 = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent swamp2 = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        harness.castSorceryReturningPermanents(player1, 0,
                Map.of(first.getId(), 1, second.getId(), 1),
                List.of(swamp1.getId(), swamp2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first, second);
        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Returning zero Swamps deals no damage")
    void returningZeroSwampsDealsNoDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        harness.castSorceryReturningPermanents(player1, 0, Map.of(), List.of());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).containsExactly(bears);
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot choose no targets when returning a Swamp for positive X")
    void cannotChooseNoTargetsForPositiveX() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        prepare();

        assertThatThrownBy(() -> harness.castSorceryReturningPermanents(player1, 0,
                Map.of(), List.of(swamp.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(swamp);
    }

    @Test
    @DisplayName("Cannot return a non-Swamp permanent")
    void cannotReturnNonSwamp() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        assertThatThrownBy(() -> harness.castSorceryReturningPermanents(player1, 0,
                Map.of(bears.getId(), 1), List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).contains(plains);
    }

    @Test
    @DisplayName("Cannot return the same Swamp more than once")
    void cannotReturnSameSwampMoreThanOnce() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        assertThatThrownBy(() -> harness.castSorceryReturningPermanents(player1, 0,
                Map.of(bears.getId(), 2), List.of(swamp.getId(), swamp.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(swamp);
    }

    @Test
    @DisplayName("Cannot return an opponent's Swamp")
    void cannotReturnOpponentSwamp() {
        Permanent opponentSwamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        assertThatThrownBy(() -> harness.castSorceryReturningPermanents(player1, 0,
                Map.of(bears.getId(), 1), List.of(opponentSwamp.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).contains(opponentSwamp);
    }

    @Test
    @DisplayName("Returns a controlled Swamp to its owner's hand")
    void returnsControlledSwampToItsOwnersHand() {
        Swamp ownedByOpponent = new Swamp();
        ownedByOpponent.setOwnerId(player2.getId());
        Permanent stolenSwamp = harness.addToBattlefieldAndReturn(player1, ownedByOpponent);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        harness.castSorceryReturningPermanents(player1, 0,
                Map.of(bears.getId(), 1), List.of(stolenSwamp.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Swamp");
        harness.assertNotInHand(player1, "Swamp");
    }

    @Test
    @DisplayName("Damage assignments must sum to the number of Swamps returned")
    void assignmentsMustSumToReturnedCount() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        assertThatThrownBy(() -> harness.castSorceryReturningPermanents(player1, 0,
                Map.of(bears.getId(), 2), List.of(swamp.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Players can't be assigned damage")
    void cannotTargetPlayers() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        prepare();

        assertThatThrownBy(() -> harness.castSorceryReturningPermanents(player1, 0,
                Map.of(player2.getId(), 1), List.of(swamp.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target noncreature permanents")
    void cannotTargetNoncreaturePermanents() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new Swamp());
        prepare();

        assertThatThrownBy(() -> harness.castSorceryReturningPermanents(player1, 0,
                Map.of(nonCreature.getId(), 1), List.of(swamp.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(swamp);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(nonCreature);
    }
}
