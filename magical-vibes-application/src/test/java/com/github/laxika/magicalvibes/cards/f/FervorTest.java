package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArdentMilitia;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArdentMilitia.class, Fervor.class, Opalescence.class})
class FervorTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have haste while Fervor is on the battlefield")
    void ownCreaturesHaveHaste() {
        harness.addToBattlefield(player1, new Fervor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArdentMilitia());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Fervor does not grant haste to opponents' creatures")
    void opponentCreaturesDoNotHaveHaste() {
        harness.addToBattlefield(player1, new Fervor());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ArdentMilitia());

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Fervor's haste grant ends when Fervor leaves the battlefield")
    void hasteGrantEndsWhenFervorLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArdentMilitia());
        Permanent fervor = harness.addToBattlefieldAndReturn(player1, new Fervor());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(fervor);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @CardUsed(Opalescence.class)
    @DisplayName("Fervor still grants haste to itself when it becomes a creature")
    void animatedFervorHasHaste() {
        Permanent fervor = harness.addToBattlefieldAndReturn(player1, new Fervor());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, fervor)).isTrue();
        assertThat(gqs.hasKeyword(gd, fervor, Keyword.HASTE)).isTrue();
    }

    @Test
    @CardUsed(Opalescence.class)
    @DisplayName("An animated Fervor also has haste when it is a creature you control")
    void fervorEnteringAfterOpalescenceHasHaste() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent fervor = harness.addToBattlefieldAndReturn(player1, new Fervor());

        assertThat(gqs.isCreature(gd, fervor)).isTrue();
        assertThat(gqs.hasKeyword(gd, fervor, Keyword.HASTE)).isTrue();
    }
}
