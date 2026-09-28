package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SelesnyaSignet;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlareOfSubdual.class, BorosRecruit.class, SelesnyaSignet.class, Forest.class})
class GlareOfSubdualTest extends BaseCardTest {

    @Test
    void tapsTargetCreatureByTappingAnUntappedCreatureYouControl() {
        Permanent glare = harness.addToBattlefieldAndReturn(player1, new GlareOfSubdual());
        Permanent costCreature = addReadyCreature(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, battlefieldIndex(player1, glare), null, target.getId());

        assertThat(costCreature.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tapsTargetArtifact() {
        Permanent glare = harness.addToBattlefieldAndReturn(player1, new GlareOfSubdual());
        addReadyCreature(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SelesnyaSignet());

        harness.activateAbility(player1, battlefieldIndex(player1, glare), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutAnUntappedCreatureToTap() {
        Permanent glare = harness.addToBattlefieldAndReturn(player1, new GlareOfSubdual());
        Permanent costCreature = addReadyCreature(player1);
        costCreature.tap();
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, glare), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotPayCostWithAnOpponentsCreature() {
        Permanent glare = harness.addToBattlefieldAndReturn(player1, new GlareOfSubdual());
        Permanent opponentCreature = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, glare), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    void canTargetAnAlreadyTappedCreature() {
        Permanent glare = harness.addToBattlefieldAndReturn(player1, new GlareOfSubdual());
        addReadyCreature(player1);
        Permanent target = addReadyCreature(player2);
        target.tap();

        harness.activateAbility(player1, battlefieldIndex(player1, glare), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotTargetALand() {
        Permanent glare = harness.addToBattlefieldAndReturn(player1, new GlareOfSubdual());
        addReadyCreature(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, glare), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new BorosRecruit());
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
