package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HyperionBlacksmith.class, AngelsFeather.class, GrizzlyBears.class})
class HyperionBlacksmithTest extends BaseCardTest {

    @Test
    void tapsOpponentArtifact() {
        addReadyBlacksmith(player1);
        Permanent target = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void untapsOpponentArtifact() {
        addReadyBlacksmith(player1);
        Permanent target = addReadyArtifact(player2);
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void mayDeclineTappingOrUntappingOpponentArtifact() {
        addReadyBlacksmith(player1);
        Permanent target = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotTargetOwnArtifact() {
        addReadyBlacksmith(player1);
        Permanent target = addReadyArtifact(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact an opponent controls");
    }

    @Test
    void cannotTargetOpponentCreature() {
        addReadyBlacksmith(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact an opponent controls");
    }

    private Permanent addReadyBlacksmith(Player player) {
        return addCreatureReady(player, new HyperionBlacksmith());
    }

    private Permanent addReadyArtifact(Player player) {
        Permanent permanent = new Permanent(new AngelsFeather());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
