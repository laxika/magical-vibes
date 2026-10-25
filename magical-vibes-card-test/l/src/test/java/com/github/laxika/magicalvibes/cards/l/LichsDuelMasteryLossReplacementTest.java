package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DoorToNothingness;
import com.github.laxika.magicalvibes.cards.e.ExquisiteArchangel;
import com.github.laxika.magicalvibes.cards.t.TheGoldenThrone;
import com.github.laxika.magicalvibes.cards.w.WheelOfSunAndMoon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lich's Duel Mastery's sacrifice replacing the life loss of a game-loss replacement that sets a life total: player
 * 1's noncreature, shieldless Mastery sits between Wheel of Sun and Moon and the opponent's Leyline of the Void.
 */
@CardUsed({LichsDuelMastery.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
class LichsDuelMasteryLossReplacementTest extends BaseCardTest {

    @Test
    @CardUsed(TheGoldenThrone.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("The Golden Throne's life total of 1 asks the Mastery before the Throne is exiled")
    void goldenThroneAsksTheMasteryBeforeItIsExiled() {
        Permanent mastery = masteryBetweenWheelAndLeyline();
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new TheGoldenThrone());
        gd.playerPoisonCounters.put(player1.getId(), 10);

        harness.runStateBasedActions();

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Exile it");
        harness.assertOnBattlefield(player1, "The Golden Throne");
        harness.assertOnBattlefield(player1, "Lich's Duel Mastery");
        harness.assertLife(player1, 20);

        harness.handleListChoice(player1, "Put it on the bottom of its owner's library");

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(throne.getCard());
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player1, 20);
    }

    @Test
    @CardUsed(TheGoldenThrone.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("An empty-library loss replaced by The Golden Throne is checked again once the Mastery is answered")
    void emptyLibraryLossIsReplacedOnceTheMasteryIsAnswered() {
        masteryBetweenWheelAndLeyline();
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new TheGoldenThrone());
        gd.playersAttemptedDrawFromEmptyLibrary.add(player1.getId());

        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Exile it");
        harness.assertOnBattlefield(player1, "The Golden Throne");
        harness.assertLife(player1, 20);

        harness.handleListChoice(player1, "Exile it");

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(throne.getCard());
        harness.assertNotOnBattlefield(player1, "Lich's Duel Mastery");
        harness.assertLife(player1, 20);
    }

    @Test
    @CardUsed(ExquisiteArchangel.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Exquisite Archangel's starting life total asks the Mastery before the Archangel is exiled")
    void exquisiteArchangelAsksTheMasteryBeforeItIsExiled() {
        masteryBetweenWheelAndLeyline();
        harness.setLife(player1, 30);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new ExquisiteArchangel());
        gd.playerPoisonCounters.put(player1.getId(), 10);

        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Exile it");
        harness.assertOnBattlefield(player1, "Exquisite Archangel");
        harness.assertLife(player1, 30);

        harness.handleListChoice(player1, "Exile it");

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(angel.getCard());
        harness.assertNotOnBattlefield(player1, "Lich's Duel Mastery");
        harness.assertLife(player1, 30);
    }

    @Test
    @CardUsed({TheGoldenThrone.class, DoorToNothingness.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("A loss an ability states, replaced by The Golden Throne, asks the Mastery before the Throne is exiled")
    void abilityLossReplacedByTheThroneAsksTheMasteryFirst() {
        Permanent mastery = masteryBetweenWheelAndLeyline();
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new TheGoldenThrone());
        Permanent door = harness.addToBattlefieldAndReturn(player2, new DoorToNothingness());
        door.untap();
        door.setSummoningSick(false);
        for (ManaColor color : java.util.List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED,
                ManaColor.GREEN)) {
            harness.addMana(player2, color, 2);
        }

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(door), null,
                player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Exile it");
        harness.assertOnBattlefield(player1, "The Golden Throne");
        harness.assertOnBattlefield(player1, "Lich's Duel Mastery");
        harness.assertLife(player1, 20);

        harness.handleListChoice(player1, "Put it on the bottom of its owner's library");

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(throne.getCard());
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player1, 20);
    }

    private Permanent masteryBetweenWheelAndLeyline() {
        harness.setLife(player1, 20);
        Permanent mastery = harness.addToBattlefieldAndReturn(player1, new LichsDuelMastery());
        harness.addToBattlefieldAndReturn(player1, new WheelOfSunAndMoon()).setAttachedTo(player1.getId());
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        return mastery;
    }
}
