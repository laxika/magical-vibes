package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinRimerunner.class, SnowCoveredMountain.class})
class GoblinRimerunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability makes the target creature unable to block this turn")
    void tapAbilityPreventsBlocking() {
        Permanent rimerunner = addCreatureReady(player1, new GoblinRimerunner());
        Permanent target = addCreatureReady(player2, new GoblinRimerunner());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(rimerunner.isTapped()).isTrue();
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Tap ability cannot target a noncreature permanent")
    void tapAbilityCannotTargetNoncreature() {
        addCreatureReady(player1, new GoblinRimerunner());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SnowCoveredMountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Snow ability grants haste until end of turn")
    void snowAbilityGrantsHaste() {
        Permanent rimerunner = addCreatureReady(player1, new GoblinRimerunner());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rimerunner, Keyword.HASTE)).isTrue();
        assertThat(rimerunner.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();

        gd.expireEndOfTurnFloatingEffects();
        rimerunner.resetModifiers();
        assertThat(gqs.hasKeyword(gd, rimerunner, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste lets the source use its tap ability while summoning sick")
    void hasteAllowsImmediateTapAbility() {
        Permanent rimerunner = harness.addToBattlefieldAndReturn(player1, new GoblinRimerunner());
        Permanent target = addCreatureReady(player2, new GoblinRimerunner());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(rimerunner.isSummoningSick()).isTrue();
        assertThat(gqs.hasKeyword(gd, rimerunner, Keyword.HASTE)).isTrue();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(rimerunner.isTapped()).isTrue();
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Tap ability prevents the target from being declared as a blocker")
    void tapAbilityPreventsBlockDeclaration() {
        addCreatureReady(player1, new GoblinRimerunner());
        addCreatureReady(player1, new GoblinRimerunner());
        Permanent target = addCreatureReady(player2, new GoblinRimerunner());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow ability")
    void regularManaCannotPaySnowAbility() {
        addCreatureReady(player1, new GoblinRimerunner());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Snow-Covered Mountain mana pays for haste while the source is tapped")
    void snowLandPaysForHasteWhileTapped() {
        Permanent rimerunner = harness.addToBattlefieldAndReturn(player1, new GoblinRimerunner());
        rimerunner.tap();
        Permanent other = addCreatureReady(player1, new GoblinRimerunner());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredMountain());

        harness.tapPermanent(player1, 2);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gqs.hasKeyword(gd, rimerunner, Keyword.HASTE)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rimerunner, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
        assertThat(rimerunner.isTapped()).isTrue();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Without haste a summoning-sick Rimerunner cannot pay the tap cost")
    void summoningSicknessPreventsTapAbility() {
        Permanent rimerunner = harness.addToBattlefieldAndReturn(player1, new GoblinRimerunner());
        Permanent target = addCreatureReady(player2, new GoblinRimerunner());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(rimerunner.isTapped()).isFalse();
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Tap ability can target its own source and expires at end of turn")
    void tapAbilityCanTargetSelfAndExpires() {
        Permanent rimerunner = addCreatureReady(player1, new GoblinRimerunner());

        harness.activateAbility(player1, 0, 0, null, rimerunner.getId());
        assertThat(rimerunner.isTapped()).isTrue();
        assertThat(rimerunner.isCantBlockThisTurn()).isFalse();
        harness.passBothPriorities();

        assertThat(rimerunner.isCantBlockThisTurn()).isTrue();

        gd.expireEndOfTurnFloatingEffects();
        rimerunner.resetModifiers();

        assertThat(rimerunner.isCantBlockThisTurn()).isFalse();
    }
}
