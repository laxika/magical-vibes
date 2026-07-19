package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ToxicIguanarTest extends BaseCardTest {

    // ===== Conditional deathtouch — controlling a green permanent =====

    @Test
    @DisplayName("Has deathtouch while controlling a green permanent")
    void hasDeathtouchWithGreenPermanent() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new ToxicIguanar());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Does NOT have deathtouch without a green permanent")
    void noDeathtouchWithoutGreenPermanent() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new ToxicIguanar());

        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A non-green permanent does not grant deathtouch")
    void nonGreenPermanentDoesNotGrant() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new ToxicIguanar());
        harness.addToBattlefield(player1, new HillGiant());

        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A green permanent controlled by an opponent does not grant deathtouch")
    void opponentGreenPermanentDoesNotGrant() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new ToxicIguanar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Gains deathtouch dynamically when a green permanent enters")
    void gainsDeathtouchDynamically() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new ToxicIguanar());

        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isFalse();

        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, iguanar, Keyword.DEATHTOUCH)).isTrue();
    }
}
