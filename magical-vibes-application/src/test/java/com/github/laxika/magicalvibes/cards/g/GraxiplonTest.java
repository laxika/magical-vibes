package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Graxiplon.class, GrizzlyBears.class, Ornithopter.class})
class GraxiplonTest extends BaseCardTest {

    @Test
    @DisplayName("Graxiplon can't be blocked by fewer than three shared-type creatures")
    void cannotBeBlockedByFewerThanThreeSharedTypeCreatures() {
        Permanent graxiplon = addAttackingGraxiplon();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, gd.playerBattlefields.get(player1.getId()).indexOf(graxiplon)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Graxiplon can be blocked when three defending creatures share a type")
    void canBeBlockedByThreeSharedTypeCreatures() {
        Permanent graxiplon = addAttackingGraxiplon();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        int graxiplonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(graxiplon);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, graxiplonIndex),
                new BlockerAssignment(1, graxiplonIndex),
                new BlockerAssignment(2, graxiplonIndex)));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::isBlocking)
                .containsExactly(true, true, true);
    }

    @Test
    @DisplayName("Three defending creatures without a shared type do not satisfy Graxiplon")
    void threeCreaturesWithoutSharedTypeDoNotSatisfyCondition() {
        Permanent graxiplon = addAttackingGraxiplon();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new Ornithopter());
        addCreatureReady(player2, new Ornithopter());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, gd.playerBattlefields.get(player1.getId()).indexOf(graxiplon)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    private Permanent addAttackingGraxiplon() {
        Permanent graxiplon = addCreatureReady(player1, new Graxiplon());
        graxiplon.setAttacking(true);
        return graxiplon;
    }
}
