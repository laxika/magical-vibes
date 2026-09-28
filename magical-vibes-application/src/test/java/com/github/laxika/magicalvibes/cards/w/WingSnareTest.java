package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WingSnare.class, WindDrake.class, GiantCockroach.class})
class WingSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Wing Snare destroys target creature with flying")
    void resolvingDestroysTargetCreature() {
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        harness.setHand(player1, List.of(new WingSnare()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, flyingCreature.getId());

        harness.assertNotOnBattlefield(player2, "Wind Drake");
        harness.assertInGraveyard(player2, "Wind Drake");
        harness.assertInGraveyard(player1, "Wing Snare");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        Permanent nonFlyingCreature = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());

        harness.setHand(player1, List.of(new WingSnare()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonFlyingCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Wing Snare fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        harness.setHand(player1, List.of(new WingSnare()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, flyingCreature.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Wing Snare");
    }
}
