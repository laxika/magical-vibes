package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.IxallisKeeper;
import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuatliWarriorPoet.class, IxallisKeeper.class, AncientBrontodon.class})
class HuatliWarriorPoetTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HuatliWarriorPoet()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castPlaneswalker(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard()).isInstanceOf(HuatliWarriorPoet.class);
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty 3")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new HuatliWarriorPoet()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        Permanent huatli = findPermanent(player1, "Huatli, Warrior Poet");
        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(huatli.isSummoningSick()).isFalse();
    }

    @Test
    @DisplayName("+2 ability gains life equal to greatest power and increases loyalty")
    void plusTwoGainsLifeEqualToGreatestPower() {
        Permanent huatli = addReadyHuatli(player1);
        harness.addToBattlefield(player1, new IxallisKeeper()); // 2/2
        harness.addToBattlefield(player1, new AncientBrontodon());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(5); // 3 + 2
        harness.assertLife(player1, lifeBefore + 9);
    }

    @Test
    @DisplayName("+2 ability gains no life when no creatures are controlled")
    void plusTwoNoCreaturesNoLifeGain() {
        Permanent huatli = addReadyHuatli(player1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(5); // 3 + 2
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("+2 ability does not count opponent's creatures")
    void plusTwoDoesNotCountOpponentCreatures() {
        addReadyHuatli(player1);
        harness.addToBattlefield(player2, new AncientBrontodon());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("0 ability creates a 3/3 green Dinosaur token with trample")
    void zeroCreatesToken() {
        Permanent huatli = addReadyHuatli(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 3 + 0

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Dinosaur"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Dinosaur token not found"));

        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.DINOSAUR);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("-X ability deals divided damage and prevents blocking")
    void minusXDealsDividedDamageAndPreventsBlocking() {
        Permanent huatli = addReadyHuatli(player1);
        harness.addToBattlefield(player2, new IxallisKeeper()); // 2/2
        harness.addToBattlefield(player2, new IxallisKeeper()); // 2/2

        List<Permanent> opponentBf = harness.getGameData().playerBattlefields.get(player2.getId());
        Permanent bear1 = opponentBf.get(0);
        Permanent bear2 = opponentBf.get(1);

        // X=2: assign 1 damage to each bear
        Map<java.util.UUID, Integer> assignments = Map.of(bear1.getId(), 1, bear2.getId(), 1);
        harness.activateAbilityWithDamageAssignments(player1, 0, 2, 2, assignments);
        harness.passBothPriorities();

        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(1); // 3 - 2

        // Both bears should still be alive (1 damage to 2/2) but can't block
        assertThat(bear1.getMarkedDamage()).isEqualTo(1);
        assertThat(bear2.getMarkedDamage()).isEqualTo(1);
        assertThat(bear1.isCantBlockThisTurn()).isTrue();
        assertThat(bear2.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("-X ability kills creature if enough damage assigned")
    void minusXKillsCreature() {
        Permanent huatli = addReadyHuatli(player1);
        harness.addToBattlefield(player2, new IxallisKeeper()); // 2/2

        Permanent bear = findPermanent(player2, "Ixalli's Keeper");

        // X=3: assign 3 damage to the bear (lethal for 2/2)
        Map<java.util.UUID, Integer> assignments = Map.of(bear.getId(), 3);
        harness.activateAbilityWithDamageAssignments(player1, 0, 2, 3, assignments);
        harness.passBothPriorities();

        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(0); // 3 - 3
        // Huatli should be in graveyard too (0 loyalty)
        harness.assertNotOnBattlefield(player1, "Huatli, Warrior Poet");

        // Bear should be dead
        harness.assertNotOnBattlefield(player2, "Ixalli's Keeper");
        harness.assertInGraveyard(player2, "Ixalli's Keeper");
    }

    @Test
    @DisplayName("-X ability cannot use more loyalty than available")
    void minusXCannotExceedLoyalty() {
        addReadyHuatli(player1);
        harness.addToBattlefield(player2, new IxallisKeeper());

        Permanent bear = findPermanent(player2, "Ixalli's Keeper");

        // X=4 but Huatli only has 3 loyalty
        Map<java.util.UUID, Integer> assignments = Map.of(bear.getId(), 4);
        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(player1, 0, 2, 4, assignments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyHuatli(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyHuatli(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    @Test
    @DisplayName("+2 determines greatest power at resolution")
    void plusTwoUsesPowerAtResolution() {
        addReadyHuatli(player1);
        harness.addToBattlefield(player1, new IxallisKeeper());
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addToBattlefield(player1, new AncientBrontodon());
        harness.passBothPriorities();
        harness.assertLife(player1, 29);
    }

    @Test
    @DisplayName("-X can be activated for zero without targets")
    void minusZeroNeedsNoTargets() {
        Permanent huatli = addReadyHuatli(player1);
        harness.activateAbilityWithDamageAssignments(player1, 0, 2, 0, Map.of());
        harness.passBothPriorities();
        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    @Test
    @DisplayName("-X does not redistribute damage from an illegal target")
    void minusXKeepsOriginalDivision() {
        addReadyHuatli(player1);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new IxallisKeeper());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new IxallisKeeper());
        harness.activateAbilityWithDamageAssignments(player1, 0, 2, 2,
                Map.of(first.getId(), 1, second.getId(), 1));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerGraveyards.get(player2.getId()).add(first.getCard());
        harness.passBothPriorities();
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        assertThat(second.isCantBlockThisTurn()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second);
    }

    @Test
    @DisplayName("Creatures whose damage is fully prevented can still block")
    void minusXPreventedDamageDoesNotPreventBlocking() {
        addReadyHuatli(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IxallisKeeper());
        creature.setDamagePreventionShield(1);
        harness.activateAbilityWithDamageAssignments(player1, 0, 2, 1, Map.of(creature.getId(), 1));
        harness.passBothPriorities();
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("-X can target a creature controlled by Huatli's controller")
    void minusXCanTargetOwnCreature() {
        addReadyHuatli(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IxallisKeeper());
        harness.activateAbilityWithDamageAssignments(player1, 0, 2, 1, Map.of(creature.getId(), 1));
        harness.passBothPriorities();
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(creature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("-X rejects player targets without paying loyalty")
    void minusXCannotTargetPlayer() {
        Permanent huatli = addReadyHuatli(player1);
        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 2, 1, Map.of(player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-X rejects assigning zero damage to a target")
    void minusXRequiresPositiveDamagePerTarget() {
        Permanent huatli = addReadyHuatli(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IxallisKeeper());
        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 2, 0, Map.of(creature.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-X requires the assigned damage to sum to X")
    void minusXRequiresCompleteDivision() {
        Permanent huatli = addReadyHuatli(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IxallisKeeper());
        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 2, 2, Map.of(creature.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyHuatli(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new HuatliWarriorPoet());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
