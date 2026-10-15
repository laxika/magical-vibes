package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SicarianInfiltrator;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodcrusherOfKhorne.class, SicarianInfiltrator.class})
class BloodcrusherOfKhorneTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have trample")
    void ownCreaturesGainTrample() {
        Permanent creature = addCreatureReady(player1, new SicarianInfiltrator());
        harness.addToBattlefield(player1, new BloodcrusherOfKhorne());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain trample")
    void opponentCreaturesDoNotGainTrample() {
        Permanent opponentCreature = addCreatureReady(player2, new SicarianInfiltrator());
        harness.addToBattlefield(player1, new BloodcrusherOfKhorne());

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample is removed when Bloodcrusher of Khorne leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent creature = addCreatureReady(player1, new SicarianInfiltrator());
        Permanent bloodcrusher = addCreatureReady(player1, new BloodcrusherOfKhorne());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(bloodcrusher);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after Bloodcrusher also gain trample")
    void laterCreaturesGainTrample() {
        harness.addToBattlefield(player1, new BloodcrusherOfKhorne());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SicarianInfiltrator());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample remains while another Bloodcrusher is on the battlefield")
    void remainingSourceKeepsGrantingTrample() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SicarianInfiltrator());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BloodcrusherOfKhorne());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BloodcrusherOfKhorne());

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }
}
