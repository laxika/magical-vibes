package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AdaptiveSporesinger;
import com.github.laxika.magicalvibes.cards.b.BloatedContaminator;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LukkaBoundToRuin.class, BloatedContaminator.class, AdaptiveSporesinger.class})
class LukkaBoundToRuinTest extends BaseCardTest {

    @Test
    @DisplayName("+1 adds red and green mana restricted to creatures")
    void plusOneAddsRestrictedCreatureMana() {
        addReadyLukka(player1, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureSpellOrAbilityMana(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.getCreatureSpellOrAbilityMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("-1 creates a toxic 3/3 Phyrexian Beast")
    void minusOneCreatesToxicBeast() {
        addReadyLukka(player1, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent beast = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Phyrexian Beast"))
                .findFirst()
                .orElseThrow();
        assertThat(beast.getCard().getPower()).isEqualTo(3);
        assertThat(beast.getCard().getToughness()).isEqualTo(3);
        assertThat(beast.getCard().getKeywords()).contains(com.github.laxika.magicalvibes.model.Keyword.TOXIC);
        assertThat(beast.getCard().getSubtypes()).containsExactlyInAnyOrder(
                com.github.laxika.magicalvibes.model.CardSubtype.PHYREXIAN,
                com.github.laxika.magicalvibes.model.CardSubtype.BEAST);
    }

    @Test
    @DisplayName("-4 uses greatest creature power at activation and can damage a planeswalker")
    void minusFourUsesGreatestPowerAndDamagesPlaneswalker() {
        Permanent lukka = addReadyLukka(player1, 5);
        Permanent largeBear = harness.addToBattlefieldAndReturn(player1, new BloatedContaminator());
        Permanent targetBear = harness.addToBattlefieldAndReturn(player2, new AdaptiveSporesinger());
        Permanent opposingLukka = harness.addToBattlefieldAndReturn(player2, new LukkaBoundToRuin());
        opposingLukka.setCounterCount(CounterType.LOYALTY, 5);
        opposingLukka.setSummoningSick(false);

        harness.activateAbilityWithDamageAssignments(player1, 0, 2, null,
                Map.of(targetBear.getId(), 2, opposingLukka.getId(), 2));
        gd.playerBattlefields.get(player1.getId()).remove(largeBear);
        harness.passBothPriorities();

        assertThat(lukka.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Adaptive Sporesinger");
        assertThat(opposingLukka.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-4 cannot target a player")
    void minusFourCannotTargetPlayer() {
        addReadyLukka(player1, 5);
        harness.addToBattlefield(player1, new BloatedContaminator());

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 2, null, Map.of(player2.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void beastToxicAppliesImmediatelyWithCombatDamage() {
        addReadyLukka(player1, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent beast = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        beast.setSummoningSick(false);
        beast.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void compleatedEntersWithThreeLoyaltyWhenLifePaysHybridSymbol() {
        harness.setHand(player1, List.of(new LukkaBoundToRuin()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        Permanent lukka = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(lukka.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void hybridSymbolCanBePaidWithGreenWithoutReducingLoyalty() {
        harness.setHand(player1, List.of(new LukkaBoundToRuin()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void minusFourCanChooseNoTargetsWithPositiveGreatestPower() {
        Permanent lukka = addReadyLukka(player1, 5);
        harness.addToBattlefield(player1, new BloatedContaminator());

        harness.activateAbilityWithDamageAssignments(player1, 0, 2, null, Map.of());
        harness.passBothPriorities();

        assertThat(lukka.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void minusFourCanChooseNoTargetsWithoutCreatures() {
        Permanent lukka = addReadyLukka(player1, 5);

        harness.activateAbilityWithDamageAssignments(player1, 0, 2, null, Map.of());
        harness.passBothPriorities();

        assertThat(lukka.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void illegalTargetDoesNotRedistributeDamageToRemainingTarget() {
        addReadyLukka(player1, 5);
        harness.addToBattlefield(player1, new BloatedContaminator());
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new AdaptiveSporesinger());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new BloatedContaminator());

        harness.activateAbilityWithDamageAssignments(player1, 0, 2, null,
                Map.of(removed.getId(), 2, remaining.getId(), 2));
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(remaining);
        assertThat(remaining.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void restrictedManaCanPayForCreatureSpell() {
        addReadyLukka(player1, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new BloatedContaminator()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bloated Contaminator");
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureSpellOrAbilityMana(ManaColor.RED)).isZero();
        assertThat(pool.getCreatureSpellOrAbilityMana(ManaColor.GREEN)).isZero();
    }

    @Test
    void restrictedManaCannotPayForPlaneswalkerSpell() {
        addReadyLukka(player1, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new LukkaBoundToRuin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castPlaneswalker(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void hybridSymbolCanBePaidWithRedWithoutReducingLoyalty() {
        harness.setHand(player1, List.of(new LukkaBoundToRuin()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private Permanent addReadyLukka(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new LukkaBoundToRuin());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
