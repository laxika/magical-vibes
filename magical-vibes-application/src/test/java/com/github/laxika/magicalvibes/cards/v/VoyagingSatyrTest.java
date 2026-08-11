package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoyagingSatyrTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps target land")
    void untapsTargetLand() {
        addCreatureReady(player1, new VoyagingSatyr());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Voyaging Satyr").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can untap an opponent's land")
    void untapsOpponentLand() {
        addCreatureReady(player1, new VoyagingSatyr());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonLand() {
        addCreatureReady(player1, new VoyagingSatyr());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
