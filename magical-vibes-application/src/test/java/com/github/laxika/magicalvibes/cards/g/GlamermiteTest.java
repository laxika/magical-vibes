package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlamermiteTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing tap taps the target creature")
    void tapsTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();

        castGlamermite(0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing untap untaps the target creature")
    void untapsTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        target.tap();

        castGlamermite(1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();
        harness.setHand(player1, List.of(new Glamermite()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castGlamermite(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new Glamermite()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, mode, targetId);
    }
}
