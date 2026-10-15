package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NestRobber;
import com.github.laxika.magicalvibes.cards.v.VraskaRelicSeeker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkNourishment.class, AirElemental.class, Forest.class, NestRobber.class, VraskaRelicSeeker.class})
class DarkNourishmentTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dark Nourishment targeting a player puts it on the stack")
    void castingTargetingPlayerPutsOnStack() {
        harness.setHand(player1, List.of(new DarkNourishment()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Dark Nourishment deals 3 damage to target player and controller gains 3 life")
    void deals3DamageToPlayerAndGains3Life() {
        harness.setHand(player1, List.of(new DarkNourishment()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Dark Nourishment deals 3 damage to target creature and kills it if toughness <= 3")
    void deals3DamageToCreatureAndKillsIt() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new NestRobber());

        harness.setHand(player1, List.of(new DarkNourishment()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        // 3 damage kills Nest Robber (1 toughness)
        harness.assertNotOnBattlefield(player2, "Nest Robber");
        // Controller gains 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Dark Nourishment deals 3 damage to creature with toughness > 3 without killing it")
    void deals3DamageToCreatureWithoutKillingIt() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new DarkNourishment()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0, elemental.getId());

        // 3 damage does not kill Air Elemental (4/4)
        harness.assertOnBattlefield(player2, "Air Elemental");
        // Controller still gains 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // Dark Nourishment carries no card-level target filter, so the effect's @ValidatesTarget
    // validator is the only thing that stops the single-targetId cast at a land.
    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DarkNourishment()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID forestId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");

        harness.assertOnBattlefield(player2, "Forest");
    }

    // Step 4 (targeting unification): the UI/AI enumeration path judges "any target" candidates by the
    // same rule as the cast path — creature/planeswalker/battle/player, never a land.
    @Test
    @DisplayName("Target enumeration excludes a land (same rule as the cast path)")
    void targetEnumerationExcludesLand() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new NestRobber());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DarkNourishment()));

        Card darkNourishment = gd.playerHands.get(player1.getId()).getFirst();
        UUID forestId = harness.getPermanentId(player2, "Forest");

        var response = harness.getValidTargetService()
                .computeValidTargetsForSpell(gd, darkNourishment, player1.getId(), null);

        assertThat(response.validPermanentIds()).contains(bear.getId()).doesNotContain(forestId);
        assertThat(response.validPlayerIds()).contains(player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("Dark Nourishment fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetCreatureRemoved() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new NestRobber());

        harness.setHand(player1, List.of(new DarkNourishment()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);

        harness.castInstant(player1, 0, bear.getId());
        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        // Spell fizzles — no life gain
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Targeting yourself deals damage and then gains the fixed life amount")
    void targetingSelfDealsDamageAndGainsLife() {
        harness.setHand(player1, List.of(new DarkNourishment()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player1, "Dark Nourishment");
    }

    @Test
    @DisplayName("Preventing all damage does not prevent the fixed three life gain")
    void gainsThreeLifeEvenWhenAllDamageIsPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NestRobber());
        target.setDamagePreventionShield(3);
        harness.setHand(player1, List.of(new DarkNourishment()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Nest Robber");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A surviving creature has exactly three damage marked")
    void marksThreeDamageOnOwnCreatureAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new DarkNourishment()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Damage to a planeswalker removes three loyalty and gains three life")
    void damagesPlaneswalkerAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VraskaRelicSeeker());
        target.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new DarkNourishment()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Vraska, Relic Seeker");
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Self-targeting at three life does not lose the game during resolution")
    void selfTargetingAtThreeLifeSurvivesResolution() {
        harness.setHand(player1, List.of(new DarkNourishment()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 3);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 3);
        assertThat(gd.gameResult).isNull();
    }
}
