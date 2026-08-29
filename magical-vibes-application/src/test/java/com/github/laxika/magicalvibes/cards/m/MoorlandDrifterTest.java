package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoorlandDrifterTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have flying without delirium")
    void noDeliriumNoFlying() {
        harness.addToBattlefield(player1, new MoorlandDrifter());

        assertThat(gqs.hasKeyword(gd, findDrifter(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has flying with four card types in its controller's graveyard")
    void deliriumGrantsFlying() {
        setDelirium();
        harness.addToBattlefield(player1, new MoorlandDrifter());

        assertThat(gqs.hasKeyword(gd, findDrifter(), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("An opponent's graveyard does not count toward delirium")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.addToBattlefield(player1, new MoorlandDrifter());

        assertThat(gqs.hasKeyword(gd, findDrifter(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses flying when its controller's graveyard drops below four card types")
    void losesFlyingWhenGraveyardChanges() {
        setDelirium();
        harness.addToBattlefield(player1, new MoorlandDrifter());

        Permanent drifter = findDrifter();
        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isTrue();

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isFalse();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }

    private Permanent findDrifter() {
        return findPermanent(player1, "Moorland Drifter");
    }
}
