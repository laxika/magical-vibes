package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinSpy;
import com.github.laxika.magicalvibes.cards.q.QuirionElves;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScarredPuma.class, GoblinSpy.class, RavenousRats.class, QuirionElves.class})
class ScarredPumaTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without a black or green creature also attacking")
    void cannotAttackWithoutMatchingCreature() {
        Permanent puma = addCreatureReady(player1, new ScarredPuma());
        Permanent goblin = addCreatureReady(player1, new GoblinSpy());
        addOpponentCreature();

        assertThatThrownBy(() -> declareAttackers(List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black or green creature");
        assertThat(puma.isAttacking()).isFalse();
        assertThat(goblin.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can attack with a black creature")
    void canAttackWithBlackCreature() {
        Permanent puma = addCreatureReady(player1, new ScarredPuma());
        Permanent blackCreature = addCreatureReady(player1, new RavenousRats());
        addOpponentCreature();

        declareAttackers(List.of(0, 1));

        assertThat(puma.isAttacking()).isTrue();
        assertThat(blackCreature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot attack when a black creature stays back")
    void cannotAttackWhenMatchingCreatureStaysBack() {
        Permanent puma = addCreatureReady(player1, new ScarredPuma());
        Permanent blackCreature = addCreatureReady(player1, new RavenousRats());
        addOpponentCreature();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black or green creature");
        assertThat(puma.isAttacking()).isFalse();
        assertThat(blackCreature.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can attack with a green creature")
    void canAttackWithGreenCreature() {
        Permanent puma = addCreatureReady(player1, new ScarredPuma());
        Permanent greenCreature = addCreatureReady(player1, new QuirionElves());
        addOpponentCreature();

        declareAttackers(List.of(0, 1));

        assertThat(puma.isAttacking()).isTrue();
        assertThat(greenCreature.isAttacking()).isTrue();
    }

    private void addOpponentCreature() {
        harness.addToBattlefield(player2, new GoblinSpy());
    }
}
