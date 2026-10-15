package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameData;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({JaceIngeniousMindMage.class, AncientBrontodon.class, Island.class})
class JaceIngeniousMindMageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new JaceIngeniousMindMage()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castPlaneswalker(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard()).isInstanceOf(JaceIngeniousMindMage.class);
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty 5")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new JaceIngeniousMindMage()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Jace, Ingenious Mind-Mage"));
        Permanent jace = bf.stream().filter(p -> p.getCard().getName().equals("Jace, Ingenious Mind-Mage")).findFirst().orElseThrow();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(jace.isSummoningSick()).isFalse();
    }

    @Test
    @DisplayName("+1 draw ability makes controller draw a card and increases loyalty")
    void plusOneDrawsCard() {
        Permanent jace = addReadyJace(player1);

        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // 5 + 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("+1 draw ability does not make opponent draw a card")
    void plusOneDoesNotDrawForOpponent() {
        addReadyJace(player1);

        int p2HandBefore = harness.getGameData().playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player2.getId())).hasSize(p2HandBefore);
    }

    @Test
    @DisplayName("+1 untap ability untaps all tapped creatures and increases loyalty")
    void plusOneUntapsCreatures() {
        Permanent jace = addReadyJace(player1);
        harness.addToBattlefield(player1, new AncientBrontodon());
        harness.addToBattlefield(player1, new AncientBrontodon());

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player1.getId());
        List<Permanent> bears = bf.stream()
                .filter(p -> p.getCard().getName().equals("Ancient Brontodon"))
                .toList();

        // Tap both creatures
        bears.forEach(Permanent::tap);
        assertThat(bears).allMatch(Permanent::isTapped);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // 5 + 1
        assertThat(bears).noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("+1 untap ability does not untap opponent's creatures")
    void plusOneDoesNotUntapOpponentCreatures() {
        addReadyJace(player1);
        harness.addToBattlefield(player2, new AncientBrontodon());

        Permanent opponentBear = findPermanent(player2, "Ancient Brontodon");
        opponentBear.tap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(opponentBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("-9 ability gains control of three target creatures")
    void minusNineGainsControlOfThreeCreatures() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 9);

        harness.addToBattlefield(player2, new AncientBrontodon());
        harness.addToBattlefield(player2, new AncientBrontodon());
        harness.addToBattlefield(player2, new AncientBrontodon());

        List<UUID> targetIds = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ancient Brontodon"))
                .map(Permanent::getId)
                .toList();
        assertThat(targetIds).hasSize(3);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, targetIds);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(0); // 9 - 9

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(jace);
        harness.assertInGraveyard(player1, "Jace, Ingenious Mind-Mage");

        // All three creatures should now be under player1's control
        for (UUID targetId : targetIds) {
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getId().equals(targetId));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getId().equals(targetId));
            assertThat(gd.newestControlEffectFor(targetId).duration()).isEqualTo(com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT);
        }
    }

    @Test
    @DisplayName("-9 ability can target fewer than three creatures")
    void minusNineCanTargetFewerThanThree() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 9);

        harness.addToBattlefield(player2, new AncientBrontodon());

        UUID bearsId = findPermanent(player2, "Ancient Brontodon").getId();

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(bearsId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bearsId));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
    }

    @Test
    @DisplayName("-9 ability can be activated with zero targets")
    void minusNineCanActivateWithZeroTargets() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 9);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(0); // 9 - 9
    }

    @Test
    @DisplayName("Cannot use -9 when loyalty is insufficient")
    void cannotActivateMinusNineWithInsufficientLoyalty() {
        addReadyJace(player1);
        // Loyalty is 5, need 9

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyJace(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyJace(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    @Test
    @DisplayName("Untap ability leaves noncreature permanents tapped")
    void untapLeavesNoncreaturesTapped() {
        Permanent jace = addReadyJace(player1);
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        jace.tap();
        island.tap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(jace.isTapped()).isTrue();
        assertThat(island.isTapped()).isTrue();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("Untap ability includes creatures that enter before resolution")
    void untapChecksBattlefieldAtResolution() {
        addReadyJace(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AncientBrontodon());
        creature.tap();

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ultimate can target your own creature and does not untap or grant haste")
    void ultimateCanTargetOwnCreatureWithoutUntapping() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 10);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AncientBrontodon());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());
        opponentCreature.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 2,
                List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .contains(ownCreature, opponentCreature);
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.isSummoningSick()).isTrue();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ultimate rejects a noncreature target")
    void ultimateCannotTargetLand() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 10);
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 2,
                List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ultimate rejects more than three targets")
    void ultimateCannotTargetFourCreatures() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 10);
        List<UUID> targets = java.util.stream.IntStream.range(0, 4)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new AncientBrontodon()).getId())
                .toList();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 2, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ultimate still gains control of remaining targets when one leaves the battlefield")
    void ultimateResolvesForRemainingLegalTargets() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 10);
        Permanent departing = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());

        harness.activateAbilityWithMultiTargets(player1, 0, 2,
                List.of(departing.getId(), remaining.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(departing);
        harness.setGraveyard(player2, List.of(departing.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(remaining).doesNotContain(departing);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(remaining);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(departing.getCard());
    }

    private Permanent addReadyJace(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new JaceIngeniousMindMage());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
