package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AggressiveMammoth.class, GrizzlyBears.class, Murder.class})
class AggressiveMammothTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have trample")
    void grantsTrampleToOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new AggressiveMammoth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Aggressive Mammoth does not grant trample to an opponent's creature")
    void doesNotGrantTrampleToOpponentsCreature() {
        harness.addToBattlefield(player1, new AggressiveMammoth());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures already on the battlefield gain trample when Mammoth enters")
    void grantsTrampleToExistingCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();

        harness.castFromHand(player1, new AggressiveMammoth(), "{3}{G}{G}{G}");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample remains until the last Mammoth leaves the battlefield")
    void losesGrantedTrampleWhenLastMammothIsDestroyed() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AggressiveMammoth());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AggressiveMammoth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.setHand(player1, List.of(new Murder(), new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castAndResolveInstant(player1, 0, first.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.castAndResolveInstant(player1, 0, second.getId());
        harness.assertNotOnBattlefield(player1, "Aggressive Mammoth");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }
}
