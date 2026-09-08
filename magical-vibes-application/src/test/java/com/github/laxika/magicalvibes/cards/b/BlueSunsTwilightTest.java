package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlueSunsTwilightTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of a creature with mana value X or less")
    void gainsPermanentControl() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castAndResolve(2, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("At X=5, creates a token copy after gaining control")
    void createsTokenCopyAtFive() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castAndResolve(5, target.getId());

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Does not create a token copy below X=5")
    void doesNotCreateTokenCopyBelowFive() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castAndResolve(4, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Rejects a creature with mana value greater than X")
    void rejectsCreatureAboveManaValueLimit() {
        Permanent target = addCreatureReady(player2, new SerraAngel());

        assertThatThrownBy(() -> {
            harness.setHand(player1, List.of(new BlueSunsTwilight()));
            harness.addMana(player1, ManaColor.BLUE, 4);
            harness.castSorcery(player1, 0, 2, target.getId());
        }).isInstanceOf(IllegalStateException.class);
    }

    private void castAndResolve(int xValue, UUID targetId) {
        harness.setHand(player1, List.of(new BlueSunsTwilight()));
        harness.addMana(player1, ManaColor.BLUE, xValue + 2);
        harness.castSorcery(player1, 0, xValue, targetId);
        harness.passBothPriorities();
    }
}
