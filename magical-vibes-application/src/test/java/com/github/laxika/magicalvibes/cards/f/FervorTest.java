package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fervor.class, GrizzlyBears.class})
class FervorTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have haste while Fervor is on the battlefield")
    void ownCreaturesHaveHaste() {
        harness.addToBattlefield(player1, new Fervor());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Fervor does not grant haste to opponents' creatures")
    void opponentCreaturesDoNotHaveHaste() {
        harness.addToBattlefield(player1, new Fervor());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isFalse();
    }

    @Test
    @CardUsed(Opalescence.class)
    @DisplayName("An animated Fervor also has haste when it is a creature you control")
    void animatedFervorHasHaste() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent fervor = harness.addToBattlefieldAndReturn(player1, new Fervor());

        assertThat(gqs.isCreature(gd, fervor)).isTrue();
        assertThat(gqs.hasKeyword(gd, fervor, Keyword.HASTE)).isTrue();
    }
}
