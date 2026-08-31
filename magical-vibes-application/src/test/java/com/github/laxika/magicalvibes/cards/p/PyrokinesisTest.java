package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GuerrillaTactics;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({Pyrokinesis.class, StormCrow.class, GuerrillaTactics.class})
class PyrokinesisTest extends BaseCardTest {

    private void addFullMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Divides 4 damage as chosen among target creatures")
    void dividesFourDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        harness.setHand(player1, List.of(new Pyrokinesis()));
        addFullMana();

        harness.castInstant(player1, 0, Map.of(first.getId(), 2, second.getId(), 2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Storm Crow");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can be cast by exiling a red card from hand instead of paying mana")
    void castByExilingRedCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        harness.setHand(player1, List.of(new Pyrokinesis(), new GuerrillaTactics()));

        harness.castInstantWithAlternateExileFromHand(player1, 0, Map.of(target.getId(), 4), 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Storm Crow");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Guerrilla Tactics");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Alternate cost rejects exiling a non-red card")
    void alternateCostRequiresRedCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        harness.setHand(player1, List.of(new Pyrokinesis(), new StormCrow()));

        assertThatThrownBy(() ->
                harness.castInstantWithAlternateExileFromHand(player1, 0, Map.of(target.getId(), 4), 1)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Players can't be assigned damage")
    void cannotTargetPlayers() {
        harness.setHand(player1, List.of(new Pyrokinesis()));
        addFullMana();

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(player2.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Assignments must sum to 4")
    void assignmentsMustSumToFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        harness.setHand(player1, List.of(new Pyrokinesis()));
        addFullMana();

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(target.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each target must receive at least one damage")
    void eachTargetMustReceiveAtLeastOneDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        harness.setHand(player1, List.of(new Pyrokinesis()));
        addFullMana();

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(first.getId(), 4, second.getId(), 0))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can assign one damage to each of four target creatures")
    void canAssignDamageToFourTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        harness.setHand(player1, List.of(new Pyrokinesis()));
        addFullMana();

        harness.castInstant(player1, 0, Map.of(
                first.getId(), 1,
                second.getId(), 1,
                third.getId(), 1,
                fourth.getId(), 1));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        assertThat(third.getMarkedDamage()).isEqualTo(1);
        assertThat(fourth.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage assigned to an illegal target is not reassigned")
    void illegalTargetDoesNotReceiveReassignedDamage() {
        Permanent illegalTarget = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        Permanent firstLegalTarget = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        Permanent secondLegalTarget = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        Permanent thirdLegalTarget = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        harness.setHand(player1, List.of(new Pyrokinesis()));
        addFullMana();

        harness.castInstant(player1, 0, Map.of(
                illegalTarget.getId(), 1,
                firstLegalTarget.getId(), 1,
                secondLegalTarget.getId(), 1,
                thirdLegalTarget.getId(), 1));
        illegalTarget.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        assertThat(illegalTarget.getMarkedDamage()).isZero();
        assertThat(firstLegalTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(secondLegalTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(thirdLegalTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when its only target becomes illegal")
    void doesNothingWhenOnlyTargetBecomesIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        harness.setHand(player1, List.of(new Pyrokinesis()));
        addFullMana();

        harness.castInstant(player1, 0, Map.of(target.getId(), 4));
        target.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Storm Crow");
        assertThat(target.getMarkedDamage()).isZero();
    }
}
