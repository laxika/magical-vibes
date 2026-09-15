package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Brawn.class, Forest.class, GrizzlyBears.class})
class BrawnTest extends BaseCardTest {

    @Test
    @DisplayName("A Brawn in the graveyard gives your creatures trample while you control a Forest")
    void grantsTrampleFromGraveyardWithForest() {
        harness.setGraveyard(player1, List.of(new Brawn()));
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
        harness.setGraveyard(player1, List.of(brawn));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();

        Forest forest = new Forest();
        harness.addToBattlefield(player1, forest);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getCard() == forest);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Brawn requires a Forest controlled by the graveyard card's controller")
    void forestMustBeControlledByGraveyardCardController() {
        harness.setGraveyard(player1, List.of(new Brawn()));
        harness.addToBattlefield(player2, new Forest());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Brawn does not grant its graveyard ability from the battlefield")
    void battlefieldBrawnDoesNotGrantGraveyardAbility() {
        harness.addToBattlefield(player1, new Brawn());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }
}
