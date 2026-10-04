package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FaerieMacabre;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gloomwidow.class, FaerieMacabre.class, SafeholdSentry.class})
class GloomwidowTest extends BaseCardTest {

    @Test
    @DisplayName("Gloomwidow can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent widowPerm = addCreatureReady(player2, new Gloomwidow());
        addCreatureReady(player1, new FaerieMacabre());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(widowPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gloomwidow cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addCreatureReady(player2, new Gloomwidow());
        addCreatureReady(player1, new SafeholdSentry());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Gloomwidow can block a creature that gained flying")
    void canBlockCreatureWithGrantedFlying() {
        Permanent widow = addCreatureReady(player2, new Gloomwidow());
        Permanent attacker = addCreatureReady(player1, new SafeholdSentry());
        attacker.getGrantedKeywords().add(Keyword.FLYING);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(widow.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Reach does not make an attacker eligible for Gloomwidow to block")
    void cannotBlockAttackerWithReachButWithoutFlying() {
        addCreatureReady(player2, new Gloomwidow());
        addCreatureReady(player1, new Gloomwidow());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Gloomwidow can block a ground creature after losing all abilities")
    void canBlockGroundCreatureAfterLosingAbilities() {
        Permanent widow = addCreatureReady(player2, new Gloomwidow());
        widow.setLosesAllAbilitiesUntilEndOfTurn(true);
        addCreatureReady(player1, new SafeholdSentry());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(widow.isBlocking()).isTrue();
    }
}
