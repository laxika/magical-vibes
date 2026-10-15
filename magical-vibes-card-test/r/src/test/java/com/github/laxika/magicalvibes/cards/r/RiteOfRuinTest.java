package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.n.NarstadScrapper;
import com.github.laxika.magicalvibes.cards.o.OtherworldAtlas;
import com.github.laxika.magicalvibes.cards.s.SigardaHostOfHerons;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiteOfRuin.class, Forest.class, GrizzlyBears.class, PithingNeedle.class,
        MoorlandInquisitor.class, NarstadScrapper.class, OtherworldAtlas.class, SigardaHostOfHerons.class})
class RiteOfRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts-creatures-lands order sacrifices 1 artifact, 2 creatures and 3 lands")
    void sacrificesOneTwoThreeInChosenOrder() {
        harness.addToBattlefield(player1, new PithingNeedle());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new RiteOfRuin()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Lands-creatures-artifacts order sacrifices only one land, sparing the other two")
    void chosenOrderChangesHowManyOfEachTypeAreLost() {
        harness.addToBattlefield(player1, new PithingNeedle());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent chosenForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new RiteOfRuin()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 5);
        harness.handleMultiplePermanentsChosen(player1, List.of(chosenForest.getId()));

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Forest", "Forest");
    }

    @Test
    @DisplayName("Every player sacrifices, not just the controller")
    void bothPlayersSacrifice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new PithingNeedle());
        harness.setHand(player1, List.of(new RiteOfRuin()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Pithing Needle");
    }

    @Test
    @DisplayName("The order is chosen during resolution, after opponents can respond")
    void choosesOrderDuringResolution() {
        harness.addToBattlefield(player1, new OtherworldAtlas());
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new RiteOfRuin()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0);
        harness.assertOnBattlefield(player1, "Otherworld Atlas");
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.isAwaitingInput()).isTrue();
        harness.assertOnBattlefield(player1, "Otherworld Atlas");
        harness.assertOnBattlefield(player1, "Moorland Inquisitor");
        harness.handleListChoice(player1, "Lands, then creatures, then artifacts");

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"0,1,2,3", "1,1,3,2", "2,2,1,3", "3,3,1,2", "4,2,3,1", "5,3,2,1"})
    @DisplayName("All six orders apply their respective sacrifice counts")
    void allOrdersSacrificeTheRequiredCounts(int order, int artifacts, int creatures, int lands) {
        for (int i = 0; i < artifacts; i++) {
            harness.addToBattlefield(player1, new OtherworldAtlas());
        }
        for (int i = 0; i < creatures; i++) {
            harness.addToBattlefield(player1, new MoorlandInquisitor());
        }
        for (int i = 0; i < lands; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new RiteOfRuin()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, order);

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .hasSize(artifacts + creatures + lands + 1);
    }

    @Test
    @DisplayName("An artifact creature sacrificed as an artifact cannot count again as a creature")
    void sacrificedArtifactCreatureIsUnavailableForLaterRound() {
        harness.addToBattlefield(player1, new NarstadScrapper());
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new RiteOfRuin()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Both players choose their creatures before any of those creatures are sacrificed")
    void sacrificesSimultaneouslyAfterBothPlayersChoose() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        Permanent spared = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        Permanent opposingFirst = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        Permanent opposingSecond = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        Permanent opposingSpared = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new RiteOfRuin()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).hasSize(3);
        harness.handleMultiplePermanentsChosen(player2, List.of(opposingFirst.getId(), opposingSecond.getId()));

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).containsExactly(spared);
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).containsExactly(opposingSpared);
    }

    @Test
    @DisplayName("Sigarda protects the opponent through all three sacrifice rounds")
    void sigardaProtectsOpponentButNotSpellController() {
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.addToBattlefield(player2, new SigardaHostOfHerons());
        harness.addToBattlefield(player2, new OtherworldAtlas());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RiteOfRuin()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).hasSize(3);
        harness.assertOnBattlefield(player2, "Sigarda, Host of Herons");
        harness.assertOnBattlefield(player2, "Otherworld Atlas");
        harness.assertOnBattlefield(player2, "Forest");
    }
}
