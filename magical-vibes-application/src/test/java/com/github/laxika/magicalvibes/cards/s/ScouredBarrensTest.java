package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScouredBarrensTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ScouredBarrens()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Scoured Barrens").isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Tapping adds white mana")
    void addsWhiteMana() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping adds black mana")
    void addsBlackMana() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    private Permanent addReadyLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ScouredBarrens());
        land.setSummoningSick(false);
        return land;
    }
}
