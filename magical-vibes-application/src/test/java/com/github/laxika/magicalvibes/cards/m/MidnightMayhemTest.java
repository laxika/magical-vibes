package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuinousGremlin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MidnightMayhem.class, RuinousGremlin.class, GrizzlyBears.class})
class MidnightMayhemTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three Gremlins and grants the keywords to own Gremlins")
    void createsGremlinsAndGrantsKeywordsToOwnGremlins() {
        Permanent existingGremlin = addCreatureReady(player1, new RuinousGremlin());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentGremlin = addCreatureReady(player2, new RuinousGremlin());

        castMidnightMayhem();

        List<Permanent> gremlinTokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(gremlinTokens).hasSize(3);
        assertThat(gremlinTokens).allSatisfy(gremlin -> assertHasMidnightMayhemKeywords(gremlin));
        assertHasMidnightMayhemKeywords(existingGremlin);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentGremlin, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentGremlin, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentGremlin, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        castMidnightMayhem();

        List<Permanent> gremlins = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(gremlins).hasSize(3);
        assertThat(gremlins).allSatisfy(gremlin -> assertHasMidnightMayhemKeywords(gremlin));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gremlins).allSatisfy(gremlin -> {
            assertThat(gqs.hasKeyword(gd, gremlin, Keyword.MENACE)).isFalse();
            assertThat(gqs.hasKeyword(gd, gremlin, Keyword.LIFELINK)).isFalse();
            assertThat(gqs.hasKeyword(gd, gremlin, Keyword.HASTE)).isFalse();
        });
    }

    private void castMidnightMayhem() {
        harness.setHand(player1, List.of(new MidnightMayhem()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void assertHasMidnightMayhemKeywords(Permanent permanent) {
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }
}
