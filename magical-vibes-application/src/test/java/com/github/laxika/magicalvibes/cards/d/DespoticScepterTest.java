package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DespoticScepter.class, DrudgeSkeletons.class, Forest.class})
class DespoticScepterTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a permanent its controller owns")
    void destroysOwnPermanent() {
        harness.addToBattlefield(player1, new DespoticScepter());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .doesNotContain(forest);
    }

    @Test
    @DisplayName("Destroyed permanent cannot be regenerated")
    void destroyedPermanentCannotRegenerate() {
        harness.addToBattlefield(player1, new DespoticScepter());
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(skeletons.getRegenerationShield()).isEqualTo(1);

        harness.activateAbility(player1, 0, null, skeletons.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .doesNotContain(skeletons);
    }

    @Test
    @DisplayName("Cannot target a permanent owned by an opponent")
    void cannotTargetOpponentPermanent() {
        harness.addToBattlefield(player1, new DespoticScepter());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you own");
    }

    @Test
    @DisplayName("Ability taps the Scepter and cannot be activated again while tapped")
    void abilityRequiresTapping() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new DespoticScepter());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, forest.getId());
        assertThat(scepter.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Permanent is already tapped");

        harness.passBothPriorities();
    }
}
