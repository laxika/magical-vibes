package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TinStreetCadet.class, GrizzlyBears.class})
class TinStreetCadetTest extends BaseCardTest {

    @Test
    @DisplayName("When Tin Street Cadet becomes blocked, it creates a 1/1 red Goblin token")
    void becomesBlockedCreatesGoblinToken() {
        Permanent cadet = addCreatureReady(player1, new TinStreetCadet());
        cadet.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertGoblinTokens(1);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures still creates only one Goblin token")
    void multipleBlockersCreateOneGoblinToken() {
        Permanent cadet = addCreatureReady(player1, new TinStreetCadet());
        cadet.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertGoblinTokens(1);
    }

    @Test
    @DisplayName("An unblocked Tin Street Cadet creates no Goblin token")
    void unblockedCreatesNoGoblinToken() {
        Permanent cadet = addCreatureReady(player1, new TinStreetCadet());
        cadet.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertGoblinTokens(0);
    }

    private void assertGoblinTokens(int expectedCount) {
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(expectedCount);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        });
    }
}
