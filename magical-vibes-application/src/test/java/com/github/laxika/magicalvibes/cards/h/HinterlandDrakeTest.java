package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HinterlandDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Hinterland Drake cannot block an artifact creature")
    void cannotBlockArtifactCreature() {
        Permanent drake = addReadyPermanent(player2, new HinterlandDrake());
        Permanent attacker = addReadyPermanent(player1, new Ornithopter());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures that aren't artifacts");
    }

    @Test
    @DisplayName("Hinterland Drake can block a nonartifact creature")
    void canBlockNonartifactCreature() {
        Permanent drake = addReadyPermanent(player2, new HinterlandDrake());
        Permanent attacker = addReadyPermanent(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(drake.isBlocking()).isTrue();
    }

    private Permanent addReadyPermanent(com.github.laxika.magicalvibes.model.Player player,
                                        com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
