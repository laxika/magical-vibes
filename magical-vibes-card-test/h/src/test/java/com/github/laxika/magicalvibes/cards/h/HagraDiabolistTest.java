package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.g.GiantScorpion;
import com.github.laxika.magicalvibes.cards.s.SeaGateLoremaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HagraDiabolist.class, SeaGateLoremaster.class, GiantScorpion.class, Disfigure.class})
class HagraDiabolistTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may make any player lose life equal to the Ally count")
    void ownAllyEntryMayMakeTargetPlayerLoseLife() {
        harness.addToBattlefield(player1, new SeaGateLoremaster());
        harness.setHand(player1, List.of(new HagraDiabolist()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Another Ally entering triggers Hagra Diabolist")
    void anotherAllyEntryTriggers() {
        harness.addToBattlefield(player1, new HagraDiabolist());
        harness.setHand(player1, List.of(new SeaGateLoremaster()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger Hagra Diabolist")
    void nonAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new HagraDiabolist());
        harness.setHand(player1, List.of(new GiantScorpion()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Declining the may ability causes no life loss")
    void mayBeDeclined() {
        harness.setHand(player1, List.of(new HagraDiabolist()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's Ally entering does not trigger Hagra Diabolist")
    void opponentAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new HagraDiabolist());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SeaGateLoremaster()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The Ally count excludes opponents' Allies and is evaluated after the source dies")
    void countsRemainingControlledAlliesWhenResolvingAfterSourceDies() {
        harness.addToBattlefield(player1, new SeaGateLoremaster());
        harness.addToBattlefield(player2, new SeaGateLoremaster());
        harness.setHand(player1, List.of(new HagraDiabolist(), new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Hagra Diabolist"));
        harness.assertInGraveyard(player1, "Hagra Diabolist");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Removing the only Ally before resolution results in zero life loss")
    void removingOnlyAllyResultsInZeroLifeLoss() {
        harness.setHand(player1, List.of(new HagraDiabolist(), new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Hagra Diabolist"));
        harness.assertInGraveyard(player1, "Hagra Diabolist");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 20);
    }
}
