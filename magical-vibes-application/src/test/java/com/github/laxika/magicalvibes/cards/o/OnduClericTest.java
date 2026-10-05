package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.k.KazanduBlademaster;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GiantScorpion;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OnduCleric.class, KazanduBlademaster.class, GiantScorpion.class, IntoTheRoil.class, Conspiracy.class})
class OnduClericTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may gain life equal to the number of Allies")
    void ownAllyEntryMayGainLifeForEachAlly() {
        harness.addToBattlefield(player1, new KazanduBlademaster());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new OnduCleric(), "{1}{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Another Ally entering triggers each Ondu Cleric")
    void anotherAllyEntryTriggersEachOnduCleric() {
        harness.addToBattlefield(player1, new OnduCleric());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new OnduCleric(), "{1}{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new OnduCleric());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new GiantScorpion(), "{2}{B}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Declining the may ability does not gain life")
    void mayBeDeclined() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new OnduCleric(), "{1}{W}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent's Ally neither triggers it nor contributes to its life gain")
    void opponentsAlliesAreExcluded() {
        harness.addToBattlefield(player1, new OnduCleric());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new OnduCleric(), "{1}{W}");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The trigger survives its source leaving and counts Allies at resolution")
    void countsAlliesAtResolutionAfterSourceLeaves() {
        harness.addToBattlefield(player1, new KazanduBlademaster());
        harness.castFromHand(player1, new OnduCleric(), "{1}{W}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, findPermanent(player1, "Ondu Cleric").getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof OnduCleric);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Its own entry triggers even when it is no longer an Ally")
    void ownEntryTriggersWithoutAllySubtype() {
        var conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);

        harness.castFromHand(player1, new OnduCleric(), "{1}{W}");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, conspiracy.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 21);
    }
}
