package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetCreaturesCantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToGreatestPowerAmongOwnCreaturesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HuatliWarriorPoetTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeLoyaltyAbilities() {
        HuatliWarriorPoet card = new HuatliWarriorPoet();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+2 ability has GainLifeEqualToGreatestPowerAmongOwnCreaturesEffect")
    void plusTwoAbilityHasCorrectEffect() {
        HuatliWarriorPoet card = new HuatliWarriorPoet();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(2);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(GainLifeEqualToGreatestPowerAmongOwnCreaturesEffect.class);
    }

    @Test
    @DisplayName("0 ability has CreateTokenEffect for 3/3 green Dinosaur with trample")
    void zeroAbilityHasCorrectEffect() {
        HuatliWarriorPoet card = new HuatliWarriorPoet();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(0);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(CreateTokenEffect.class);
    }

    @Test
    @DisplayName("-X ability has DealXDamageDividedAmongTargetCreaturesCantBlockEffect and is variable loyalty cost")
    void minusXAbilityHasCorrectEffect() {
        HuatliWarriorPoet card = new HuatliWarriorPoet();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.isVariableLoyaltyCost()).isTrue();
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(DealXDamageDividedAmongTargetCreaturesCantBlockEffect.class);
    }

    // ===== Casting =====

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
        assertThat(entry.getCard().getName()).isEqualTo("Huatli, Warrior Poet");
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty 3")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new HuatliWarriorPoet()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Huatli, Warrior Poet"));
        Permanent huatli = bf.stream().filter(p -> p.getCard().getName().equals("Huatli, Warrior Poet")).findFirst().orElseThrow();
        assertThat(huatli.getLoyaltyCounters()).isEqualTo(3);
        assertThat(huatli.isSummoningSick()).isFalse();
    }

    // ===== +2 ability: You gain life equal to the greatest power among creatures you control =====

    @Test
    @DisplayName("+2 ability gains life equal to greatest power and increases loyalty")
    void plusTwoGainsLifeEqualToGreatestPower() {
        Permanent huatli = addReadyHuatli(player1);
        // Add a 2/2 creature
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        // Add a bigger creature directly
        GrizzlyBears bigCard = new GrizzlyBears();
        bigCard.setPower(4);
        bigCard.setToughness(4);
        Permanent big = new Permanent(bigCard);
        big.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(big);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(huatli.getLoyaltyCounters()).isEqualTo(5); // 3 + 2
        int lifeAfter = gd.playerLifeTotals.get(player1.getId());
        assertThat(lifeAfter).isEqualTo(lifeBefore + 4); // greatest power is 4
    }

    @Test
    @DisplayName("+2 ability gains no life when no creatures are controlled")
    void plusTwoNoCreaturesNoLifeGain() {
        Permanent huatli = addReadyHuatli(player1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(huatli.getLoyaltyCounters()).isEqualTo(5); // 3 + 2
        int lifeAfter = gd.playerLifeTotals.get(player1.getId());
        assertThat(lifeAfter).isEqualTo(lifeBefore); // no creatures = no life gain
    }

    @Test
    @DisplayName("+2 ability does not count opponent's creatures")
    void plusTwoDoesNotCountOpponentCreatures() {
        Permanent huatli = addReadyHuatli(player1);
        GrizzlyBears bigCard = new GrizzlyBears();
        bigCard.setPower(7);
        bigCard.setToughness(7);
        Permanent opponentBig = new Permanent(bigCard);
        opponentBig.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(opponentBig);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int lifeAfter = gd.playerLifeTotals.get(player1.getId());
        assertThat(lifeAfter).isEqualTo(lifeBefore); // opponent creatures don't count
    }

    // ===== 0 ability: Create a 3/3 green Dinosaur creature token with trample =====

    @Test
    @DisplayName("0 ability creates a 3/3 green Dinosaur token with trample")
    void zeroCreatesToken() {
        Permanent huatli = addReadyHuatli(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(huatli.getLoyaltyCounters()).isEqualTo(3); // 3 + 0

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Dinosaur"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Dinosaur token not found"));

        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
    }

    // ===== -X ability: X damage divided among creatures, can't block =====

    @Test
    @DisplayName("-X ability deals divided damage and prevents blocking")
    void minusXDealsDividedDamageAndPreventsBlocking() {
        Permanent huatli = addReadyHuatli(player1);
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        List<Permanent> opponentBf = harness.getGameData().playerBattlefields.get(player2.getId());
        Permanent bear1 = opponentBf.stream().filter(p -> p.getCard().getName().equals("Grizzly Bears")).findFirst().orElseThrow();
        Permanent bear2 = opponentBf.stream().filter(p -> p.getCard().getName().equals("Grizzly Bears") && p != bear1).findFirst().orElseThrow();

        // X=2: assign 1 damage to each bear
        Map<java.util.UUID, Integer> assignments = Map.of(bear1.getId(), 1, bear2.getId(), 1);
        harness.activateAbilityWithDamageAssignments(player1, 0, 2, 2, assignments);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(huatli.getLoyaltyCounters()).isEqualTo(1); // 3 - 2

        // Both bears should still be alive (1 damage to 2/2) but can't block
        assertThat(bear1.isCantBlockThisTurn()).isTrue();
        assertThat(bear2.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("-X ability kills creature if enough damage assigned")
    void minusXKillsCreature() {
        Permanent huatli = addReadyHuatli(player1);
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // X=3: assign 3 damage to the bear (lethal for 2/2)
        Map<java.util.UUID, Integer> assignments = Map.of(bear.getId(), 3);
        harness.activateAbilityWithDamageAssignments(player1, 0, 2, 3, assignments);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(huatli.getLoyaltyCounters()).isEqualTo(0); // 3 - 3
        // Huatli should be in graveyard too (0 loyalty)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Huatli, Warrior Poet"));

        // Bear should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("-X ability cannot use more loyalty than available")
    void minusXCannotExceedLoyalty() {
        Permanent huatli = addReadyHuatli(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // X=4 but Huatli only has 3 loyalty
        Map<java.util.UUID, Integer> assignments = Map.of(bear.getId(), 4);
        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(player1, 0, 2, 4, assignments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Loyalty ability restrictions =====

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

    // ===== Helpers =====

    private Permanent addReadyHuatli(Player player) {
        HuatliWarriorPoet card = new HuatliWarriorPoet();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(3);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
