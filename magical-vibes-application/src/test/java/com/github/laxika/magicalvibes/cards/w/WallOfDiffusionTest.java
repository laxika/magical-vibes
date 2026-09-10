package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Fireslinger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfDiffusion.class, Fireslinger.class})
class WallOfDiffusionTest extends BaseCardTest {

    private Permanent attacker(Card card, boolean shadow) {
        if (shadow) {
            card.setKeywords(Set.of(Keyword.SHADOW));
        }
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setAttacking(true);
        return permanent;
    }

    @Test
    @DisplayName("Wall of Diffusion can block a creature with shadow")
    void blocksShadowAttacker() {
        Permanent wall = addCreatureReady(player2, new WallOfDiffusion());
        attacker(new Fireslinger(), true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Wall of Diffusion still blocks creatures without shadow")
    void blocksNormalAttacker() {
        Permanent wall = addCreatureReady(player2, new WallOfDiffusion());
        attacker(new Fireslinger(), false);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without the ability still can't block a creature with shadow")
    void plainBlockerCannotBlockShadow() {
        addCreatureReady(player2, new Fireslinger());
        attacker(new Fireslinger(), true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
