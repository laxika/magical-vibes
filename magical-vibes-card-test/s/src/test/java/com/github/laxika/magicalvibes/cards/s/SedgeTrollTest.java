package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.w.Weakness;
import com.github.laxika.magicalvibes.cards.u.UndergroundSea;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SedgeTroll.class, Swamp.class, Forest.class, UndergroundSea.class, HillGiant.class,
        LightningBolt.class, Terror.class, Weakness.class})
class SedgeTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Sedge Troll is 2/2 without a Swamp")
    void baseStatsWithoutSwamp() {
        harness.addToBattlefield(player1, new SedgeTroll());

        assertStats(2, 2);
    }

    @Test
    @DisplayName("A non-Swamp land does not grant the boost")
    void nonSwampLandDoesNotGrantBoost() {
        harness.addToBattlefield(player1, new SedgeTroll());
        harness.addToBattlefield(player1, new Forest());

        assertStats(2, 2);
    }

    @Test
    @DisplayName("Sedge Troll gets +1/+1 while its controller controls a Swamp")
    void getsBoostWithSwamp() {
        harness.addToBattlefield(player1, new SedgeTroll());
        harness.addToBattlefield(player1, new Swamp());

        assertStats(3, 3);
    }

    @Test
    void getsBoostWithSwampSubtype() {
        harness.addToBattlefield(player1, new SedgeTroll());
        harness.addToBattlefield(player1, new UndergroundSea());

        assertStats(3, 3);
    }

    @Test
    void losesBoostWhenSwampLeavesBattlefield() {
        harness.addToBattlefield(player1, new SedgeTroll());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        assertStats(3, 3);

        gd.playerBattlefields.get(player1.getId()).remove(swamp);

        assertStats(2, 2);
    }

    @Test
    @DisplayName("An opponent's Swamp does not grant the boost")
    void opponentSwampDoesNotGrantBoost() {
        harness.addToBattlefield(player1, new SedgeTroll());
        harness.addToBattlefield(player2, new Swamp());

        assertStats(2, 2);
    }

    @Test
    @DisplayName("Paying {B} grants Sedge Troll a regeneration shield")
    void payingBlackGrantsRegenerationShield() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new SedgeTroll());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void cannotActivateWithOnlyNonBlackMana() {
        harness.addToBattlefield(player1, new SedgeTroll());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A regeneration shield saves Sedge Troll from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new SedgeTroll());
        troll.setRegenerationShield(1);
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Sedge Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isZero();
        assertThat(troll.getMarkedDamage()).isZero();
    }

    @Test
    void multipleTappedSwampsGrantOnlyOneBoostToTheTroll() {
        harness.addToBattlefield(player1, new SedgeTroll());
        harness.addToBattlefieldAndReturn(player1, new Swamp()).tap();
        harness.addToBattlefieldAndReturn(player1, new Swamp()).tap();
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        assertStats(3, 3);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    void creatingShieldDoesNotTapOrRemoveDamage() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new SedgeTroll());
        troll.setMarkedDamage(1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(troll.getRegenerationShield()).isZero();
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
        assertThat(troll.isTapped()).isFalse();
        assertThat(troll.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void regenerationCanBeActivatedWhileTappedAndWithoutASwamp() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new SedgeTroll());
        troll.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
        assertThat(troll.isTapped()).isTrue();
    }

    @Test
    void shieldSavesFromOneLethalSpellButNotTheNext() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new SedgeTroll());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, troll.getId());

        harness.assertOnBattlefield(player1, "Sedge Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getMarkedDamage()).isZero();
        assertThat(troll.getRegenerationShield()).isZero();

        harness.castAndResolveInstant(player2, 0, troll.getId());

        harness.assertNotOnBattlefield(player1, "Sedge Troll");
        harness.assertInGraveyard(player1, "Sedge Troll");
    }

    @Test
    void terrorPreventsRegenerationDespiteAnExistingShield() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new SedgeTroll());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0, troll.getId());

        harness.assertNotOnBattlefield(player1, "Sedge Troll");
        harness.assertInGraveyard(player1, "Sedge Troll");
    }

    @Test
    void regenerationDoesNotSaveFromZeroToughness() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new SedgeTroll());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Weakness(), new Weakness()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castEnchantment(player2, 0, troll.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sedge Troll");
        harness.castEnchantment(player2, 0, troll.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sedge Troll");
        harness.assertInGraveyard(player1, "Sedge Troll");
    }

    private void assertStats(int power, int toughness) {
        Permanent troll = findPermanent(player1, "Sedge Troll");
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(toughness);
    }
}
