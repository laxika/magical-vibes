package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tremble.class, Mountain.class, Forest.class, AvenFlock.class})
class TrembleTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices a land when they control only one")
    void eachPlayerSacrificesOneLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        cast();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("A player with no lands sacrifices nothing")
    void playerWithNoLandsSacrificesNothing() {
        harness.addToBattlefield(player2, new Forest());

        cast();

        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Each player chooses which land to sacrifice")
    void eachPlayerChoosesLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        cast();

        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent p1Mountain = findPermanent(player1, "Mountain");
        harness.handleMultiplePermanentsChosen(player1, List.of(p1Mountain.getId()));

        choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        Permanent p2Forest = findPermanent(player2, "Forest");
        harness.handleMultiplePermanentsChosen(player2, List.of(p2Forest.getId()));

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Only lands are sacrificed")
    void onlyLandsAreSacrificed() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new AvenFlock());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new AvenFlock());

        cast();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Aven Flock");
        harness.assertOnBattlefield(player2, "Aven Flock");
    }

    private void cast() {
        harness.castFromHand(player1, new Tremble(), "{1}{R}");
        harness.passBothPriorities();
    }
}
