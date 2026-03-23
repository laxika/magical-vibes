package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllControlledPermanentsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JaceIngeniousMindMageTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeLoyaltyAbilities() {
        JaceIngeniousMindMage card = new JaceIngeniousMindMage();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+1 draw ability has DrawCardEffect(1)")
    void plusOneDrawAbilityHasCorrectEffect() {
        JaceIngeniousMindMage card = new JaceIngeniousMindMage();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(1);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(DrawCardEffect.class);
        assertThat(((DrawCardEffect) ability.getEffects().getFirst()).amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 untap ability has UntapAllControlledPermanentsEffect with creature filter")
    void plusOneUntapAbilityHasCorrectEffect() {
        JaceIngeniousMindMage card = new JaceIngeniousMindMage();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(1);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(UntapAllControlledPermanentsEffect.class);
        UntapAllControlledPermanentsEffect effect = (UntapAllControlledPermanentsEffect) ability.getEffects().getFirst();
        assertThat(effect.filter()).isNotNull();
    }

    @Test
    @DisplayName("-9 ability has GainControlOfTargetPermanentEffect with up-to-three targeting")
    void minusNineAbilityHasCorrectEffect() {
        JaceIngeniousMindMage card = new JaceIngeniousMindMage();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-9);
        assertThat(ability.getMinTargets()).isZero();
        assertThat(ability.getMaxTargets()).isEqualTo(3);
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(GainControlOfTargetPermanentEffect.class);
    }

    // ===== Casting =====

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
        assertThat(entry.getCard().getName()).isEqualTo("Jace, Ingenious Mind-Mage");
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
        assertThat(jace.getLoyaltyCounters()).isEqualTo(5);
        assertThat(jace.isSummoningSick()).isFalse();
    }

    // ===== +1 ability: Draw a card =====

    @Test
    @DisplayName("+1 draw ability makes controller draw a card and increases loyalty")
    void plusOneDrawsCard() {
        Permanent jace = addReadyJace(player1);

        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getLoyaltyCounters()).isEqualTo(6); // 5 + 1
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

    // ===== +1 ability: Untap all creatures you control =====

    @Test
    @DisplayName("+1 untap ability untaps all tapped creatures and increases loyalty")
    void plusOneUntapsCreatures() {
        Permanent jace = addReadyJace(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player1.getId());
        List<Permanent> bears = bf.stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .toList();

        // Tap both creatures
        bears.forEach(Permanent::tap);
        assertThat(bears).allMatch(Permanent::isTapped);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(jace.getLoyaltyCounters()).isEqualTo(6); // 5 + 1
        assertThat(bears).noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("+1 untap ability does not untap opponent's creatures")
    void plusOneDoesNotUntapOpponentCreatures() {
        addReadyJace(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBear = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        opponentBear.tap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(opponentBear.isTapped()).isTrue();
    }

    // ===== −9 ability: Gain control of up to three target creatures =====

    @Test
    @DisplayName("-9 ability gains control of three target creatures")
    void minusNineGainsControlOfThreeCreatures() {
        Permanent jace = addReadyJace(player1);
        jace.setLoyaltyCounters(9);

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        List<UUID> targetIds = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .map(Permanent::getId)
                .toList();
        assertThat(targetIds).hasSize(3);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, targetIds);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getLoyaltyCounters()).isEqualTo(0); // 9 - 9

        // All three creatures should now be under player1's control
        for (UUID targetId : targetIds) {
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getId().equals(targetId));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getId().equals(targetId));
            assertThat(gd.permanentControlStolenCreatures).contains(targetId);
        }
    }

    @Test
    @DisplayName("-9 ability can target fewer than three creatures")
    void minusNineCanTargetFewerThanThree() {
        Permanent jace = addReadyJace(player1);
        jace.setLoyaltyCounters(9);

        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow().getId();

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
        jace.setLoyaltyCounters(9);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of());
        harness.passBothPriorities();

        assertThat(jace.getLoyaltyCounters()).isEqualTo(0); // 9 - 9
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

    // ===== Loyalty ability restrictions =====

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

    // ===== Helpers =====

    private Permanent addReadyJace(Player player) {
        JaceIngeniousMindMage card = new JaceIngeniousMindMage();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(5);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
