package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeatherbackBaloth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmperorApatzecIntliIV.class, LeatherbackBaloth.class, HillGiant.class, GrizzlyBears.class})
class EmperorApatzecIntliIVTest extends BaseCardTest {

    @Test
    @DisplayName("A creature with power and toughness at least 4 gains perpetual haste and grants 4 life")
    void highPowerAndToughnessCreatureGainsHasteAndLife() {
        harness.addToBattlefield(player1, new EmperorApatzecIntliIV());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new LeatherbackBaloth());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, entering, Keyword.HASTE)).isTrue();
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("A creature with mana value at least 4 causes a creature to be sought")
    void highManaValueCreatureCausesCreatureSeek() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new EmperorApatzecIntliIV());

        harness.enterBattlefieldAndReturn(player1, new HillGiant());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A creature below all thresholds does not receive any reward")
    void lowPowerToughnessAndManaValueCreatureDoesNotTrigger() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new EmperorApatzecIntliIV());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, entering, Keyword.HASTE)).isFalse();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
