package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MazeBehemoth;
import com.github.laxika.magicalvibes.cards.m.MazeGlider;
import com.github.laxika.magicalvibes.cards.m.MendingTouch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AscendedLawmage.class, MazeBehemoth.class, MazeGlider.class, MendingTouch.class})
class AscendedLawmageTest extends BaseCardTest {

    @Test
    void opponentCannotTargetLawmage() {
        Permanent lawmage = addCreatureReady(player1, new AscendedLawmage());
        harness.setHand(player2, List.of(new MendingTouch()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, lawmage.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void controllerCanTargetLawmage() {
        Permanent lawmage = addCreatureReady(player1, new AscendedLawmage());
        harness.setHand(player1, List.of(new MendingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, lawmage.getId());

        assertThat(lawmage.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void groundCreatureCannotBlockLawmage() {
        addCreatureReady(player1, new AscendedLawmage());
        addCreatureReady(player2, new MazeBehemoth());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlockLawmageDespiteHexproof() {
        addCreatureReady(player1, new AscendedLawmage());
        Permanent blocker = addCreatureReady(player2, new MazeGlider());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
