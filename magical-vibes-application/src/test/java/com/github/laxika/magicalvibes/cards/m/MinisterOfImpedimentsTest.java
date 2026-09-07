package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinisterOfImpediments.class, GrizzlyBears.class, Forest.class})
class MinisterOfImpedimentsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping the Minister puts its ability on the stack")
    void tappingMinisterActivatesAbility() {
        Permanent minister = addReadyMinister(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(minister.isTapped()).isTrue();
        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability taps target creature")
    void tapsTargetCreature() {
        addReadyMinister(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability can target a creature its controller controls")
    void tapsOwnCreature() {
        addReadyMinister(player1);
        Permanent target = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot target a land")
    void cannotTargetLand() {
        addReadyMinister(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability cannot be activated while the Minister has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent minister = new Permanent(new MinisterOfImpediments());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(minister);
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    private Permanent addReadyMinister(Player player) {
        Permanent minister = new Permanent(new MinisterOfImpediments());
        minister.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(minister);
        return minister;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
