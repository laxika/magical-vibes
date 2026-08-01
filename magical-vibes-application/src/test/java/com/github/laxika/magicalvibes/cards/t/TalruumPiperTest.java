package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TalruumPiperTest extends BaseCardTest {

    @Test
    @DisplayName("Flying creatures must block Talruum Piper; ground creatures are not forced")
    void flyingMustBlockGroundNotForced() {
        Permanent piper = attackingCreature(new TalruumPiper());
        gd.playerBattlefields.get(player1.getId()).add(piper);

        Permanent flyer = readyCreature(new SerraAngel());
        Permanent ground = readyCreature(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(flyer);
        gd.playerBattlefields.get(player2.getId()).add(ground);

        prepareDeclareBlockers();

        // Empty blocks — flyer must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        // Only ground blocks — flyer still must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        // Flyer alone — ground is not forced
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(flyer.isBlocking()).isTrue();
        assertThat(ground.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("No blockers required when defender has only ground creatures")
    void onlyGroundCreaturesEmptyBlocksOk() {
        Permanent piper = attackingCreature(new TalruumPiper());
        gd.playerBattlefields.get(player1.getId()).add(piper);

        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isFalse();
    }

    @Test
    @DisplayName("All able flying creatures must block Talruum Piper")
    void twoFlyersMustBothBlock() {
        Permanent piper = attackingCreature(new TalruumPiper());
        gd.playerBattlefields.get(player1.getId()).add(piper);

        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new SerraAngel()));
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new SerraAngel()));

        prepareDeclareBlockers();

        // Only one flyer assigned — should fail because both must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        // Both flyers assigned — should succeed
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    private Permanent attackingCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }

    private Permanent readyCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
