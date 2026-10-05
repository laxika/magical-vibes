package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JacesScrutiny;
import com.github.laxika.magicalvibes.cards.w.WickerWitch;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoorlandDrifter.class, Forest.class, JacesScrutiny.class, WickerWitch.class})
class MoorlandDrifterTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have flying without delirium")
    void noDeliriumNoFlying() {
        Permanent drifter = harness.addToBattlefieldAndReturn(player1, new MoorlandDrifter());

        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has flying with four card types in its controller's graveyard")
    void deliriumGrantsFlying() {
        setDelirium();
        Permanent drifter = harness.addToBattlefieldAndReturn(player1, new MoorlandDrifter());

        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("An opponent's graveyard does not count toward delirium")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(
                new MoorlandDrifter(), new Forest(), new JacesScrutiny(), new WickerWitch()));
        Permanent drifter = harness.addToBattlefieldAndReturn(player1, new MoorlandDrifter());

        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses flying when its controller's graveyard drops below four card types")
    void losesFlyingWhenGraveyardChanges() {
        setDelirium();
        Permanent drifter = harness.addToBattlefieldAndReturn(player1, new MoorlandDrifter());
        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isTrue();

        harness.setGraveyard(player1, List.of(new MoorlandDrifter(), new Forest(), new JacesScrutiny()));

        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gains flying immediately when a fourth card type reaches the graveyard")
    void gainsFlyingWhileOnBattlefield() {
        harness.setGraveyard(player1, List.of(new MoorlandDrifter(), new Forest(), new JacesScrutiny()));
        Permanent drifter = harness.addToBattlefieldAndReturn(player1, new MoorlandDrifter());
        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isFalse();

        setDelirium();

        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Four cards with only three distinct card types do not grant flying")
    void duplicateCardTypesDoNotCountTwice() {
        harness.setGraveyard(player1, List.of(
                new MoorlandDrifter(), new MoorlandDrifter(), new Forest(), new JacesScrutiny()));
        Permanent drifter = harness.addToBattlefieldAndReturn(player1, new MoorlandDrifter());

        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An artifact creature counts as two types and flying is granted only to the Drifter")
    void multipleTypesOnOneCardCountSeparately() {
        harness.setGraveyard(player1, List.of(new WickerWitch(), new Forest(), new JacesScrutiny()));
        Permanent drifter = harness.addToBattlefieldAndReturn(player1, new MoorlandDrifter());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new WickerWitch());
        Permanent opposingDrifter = harness.addToBattlefieldAndReturn(player2, new MoorlandDrifter());

        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingDrifter, Keyword.FLYING)).isFalse();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new MoorlandDrifter(), new Forest(), new JacesScrutiny(), new WickerWitch()));
    }

}
