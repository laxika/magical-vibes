package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AzoriusChancery;
import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinisterOfImpediments.class, AzoriusFirstWing.class, AzoriusChancery.class})
class MinisterOfImpedimentsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping the Minister puts its ability on the stack")
    void tappingMinisterActivatesAbility() {
        Permanent minister = addCreatureReady(player1, new MinisterOfImpediments());
        Permanent target = addCreatureReady(player2, new AzoriusFirstWing());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(minister.isTapped()).isTrue();
        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability taps target creature")
    void tapsTargetCreature() {
        addCreatureReady(player1, new MinisterOfImpediments());
        Permanent target = addCreatureReady(player2, new AzoriusFirstWing());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability can target a creature its controller controls")
    void tapsOwnCreature() {
        addCreatureReady(player1, new MinisterOfImpediments());
        Permanent target = addCreatureReady(player1, new AzoriusFirstWing());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability can target the Minister itself")
    void tapsItself() {
        Permanent minister = addCreatureReady(player1, new MinisterOfImpediments());

        harness.activateAbility(player1, 0, null, minister.getId());
        harness.passBothPriorities();

        assertThat(minister.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new MinisterOfImpediments());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AzoriusChancery());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability cannot be activated while the Minister has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new MinisterOfImpediments());
        Permanent target = addCreatureReady(player2, new AzoriusFirstWing());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Ability can target an already tapped creature")
    void canTargetTappedCreature() {
        Permanent minister = addCreatureReady(player1, new MinisterOfImpediments());
        Permanent target = addCreatureReady(player2, new AzoriusFirstWing());
        target.setTapped(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(minister.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Minister cannot pay the tap cost")
    void cannotActivateWhileTapped() {
        Permanent minister = addCreatureReady(player1, new MinisterOfImpediments());
        Permanent target = addCreatureReady(player2, new AzoriusFirstWing());
        minister.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability resolves after the Minister leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent minister = addCreatureReady(player1, new MinisterOfImpediments());
        Permanent target = addCreatureReady(player2, new AzoriusFirstWing());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(minister);
        gd.playerGraveyards.get(player1.getId()).add(minister.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

}
