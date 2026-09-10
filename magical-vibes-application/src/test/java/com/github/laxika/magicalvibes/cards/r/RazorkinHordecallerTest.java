package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazorkinHordecaller.class, GrizzlyBears.class})
class RazorkinHordecallerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a 1/1 red Gremlin token")
    void attackingCreatesGremlinToken() {
        addCreatureReady(player1, new RazorkinHordecaller());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Gremlin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttackedThisTurn()).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a token when you do not attack")
    void noTokenWhenNotAttacking() {
        addCreatureReady(player1, new RazorkinHordecaller());

        declareAttackers(List.of());

        assertThat(findPermanents(player1, "Gremlin")).isEmpty();
    }
}
