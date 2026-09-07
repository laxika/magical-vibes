package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Brawn.class, Forest.class, GrizzlyBears.class})
class BrawnTest extends BaseCardTest {

    @Test
    @DisplayName("A Brawn in the graveyard gives your creatures trample while you control a Forest")
    void grantsTrampleFromGraveyardWithForest() {
        gd.playerGraveyards.get(player1.getId()).add(new Brawn());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Brawn's graveyard ability turns off without a Forest or after Brawn leaves the graveyard")
    void graveyardAbilityTurnsOffWhenConditionChanges() {
        Brawn brawn = new Brawn();
        gd.playerGraveyards.get(player1.getId()).add(brawn);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();

        Forest forest = new Forest();
        harness.addToBattlefield(player1, forest);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getCard() == forest);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();

        gd.playerGraveyards.get(player1.getId()).remove(brawn);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }
}
