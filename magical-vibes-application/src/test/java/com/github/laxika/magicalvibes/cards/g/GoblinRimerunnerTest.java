package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinRimerunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability makes the target creature unable to block this turn")
    void tapAbilityPreventsBlocking() {
        Permanent rimerunner = addCreatureReady(player1, new GoblinRimerunner());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(rimerunner.isTapped()).isTrue();
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Tap ability cannot target a noncreature permanent")
    void tapAbilityCannotTargetNoncreature() {
        addCreatureReady(player1, new GoblinRimerunner());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

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

        assertThat(rimerunner.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();

        rimerunner.resetModifiers();
        assertThat(rimerunner.hasKeyword(Keyword.HASTE)).isFalse();
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
}
