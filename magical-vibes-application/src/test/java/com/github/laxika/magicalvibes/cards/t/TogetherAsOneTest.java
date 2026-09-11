package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TogetherAsOneTest extends BaseCardTest {

    @Test
    @DisplayName("Converge counts distinct colored mana and resolves all three effects")
    void resolvesDrawDamageAndLifeGain() {
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new TogetherAsOne()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, List.of(player2.getId(), damageTarget.getId()));
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Repeated mana of one color counts only once for Converge")
    void repeatedColorCountsOnce() {
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(new TogetherAsOne()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, List.of(player2.getId(), damageTarget.getId()));
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(damageTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rejects a non-player draw target")
    void rejectsNonPlayerDrawTarget() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TogetherAsOne()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(permanent.getId(), permanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
