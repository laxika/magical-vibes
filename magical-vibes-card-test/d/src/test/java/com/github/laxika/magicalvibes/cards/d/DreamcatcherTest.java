package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.cards.w.WheelOfSunAndMoon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dreamcatcher.class, SpiritualVisit.class, ArabaMothrider.class})
class DreamcatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger sacrifices Dreamcatcher and draws a card for an Arcane spell")
    void acceptingArcaneTriggerSacrificesAndDraws() {
        Permanent dreamcatcher = addDreamcatcher();
        harness.setLibrary(player1, List.of(new ArabaMothrider()));

        harness.castFromHand(player1, new SpiritualVisit(), "{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dreamcatcher);
        harness.assertInGraveyard(player1, "Dreamcatcher");
        harness.assertInHand(player1, "Araba Mothrider");
    }

    @Test
    @DisplayName("Accepting the trigger sacrifices Dreamcatcher and draws a card for a Spirit spell")
    void acceptingSpiritTriggerSacrificesAndDraws() {
        Permanent dreamcatcher = addDreamcatcher();
        harness.setLibrary(player1, List.of(new ArabaMothrider()));

        harness.castFromHand(player1, new Dreamcatcher(), "{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dreamcatcher);
        harness.assertInGraveyard(player1, "Dreamcatcher");
        harness.assertInHand(player1, "Araba Mothrider");
    }

    @Test
    @DisplayName("Declining the trigger keeps Dreamcatcher and draws no card")
    void decliningTriggerKeepsDreamcatcherAndDoesNotDraw() {
        Permanent dreamcatcher = addDreamcatcher();
        harness.setLibrary(player1, List.of(new ArabaMothrider()));

        harness.castFromHand(player1, new SpiritualVisit(), "{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dreamcatcher);
        harness.assertNotInGraveyard(player1, "Dreamcatcher");
        harness.assertNotInHand(player1, "Araba Mothrider");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger Dreamcatcher")
    void unrelatedSpellDoesNotTrigger() {
        Permanent dreamcatcher = addDreamcatcher();
        harness.setLibrary(player1, List.of(new ArabaMothrider()));

        harness.castFromHand(player1, new ArabaMothrider(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dreamcatcher);
        harness.assertNotInHand(player1, "Araba Mothrider");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The sacrifice choice is made when the trigger resolves, after players can respond")
    void sacrificeChoiceWaitsUntilTriggerResolution() {
        Permanent dreamcatcher = addDreamcatcher();
        harness.setLibrary(player1, List.of(new ArabaMothrider()));

        harness.castFromHand(player1, new SpiritualVisit(), "{W}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dreamcatcher);
        harness.assertNotInHand(player1, "Araba Mothrider");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Dreamcatcher");
        harness.assertInGraveyard(player1, "Dreamcatcher");
        harness.assertInHand(player1, "Araba Mothrider");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("An opponent's Arcane spell does not trigger Dreamcatcher")
    void opponentsArcaneSpellDoesNotTrigger() {
        Permanent dreamcatcher = addDreamcatcher();
        harness.setLibrary(player1, List.of(new ArabaMothrider()));

        harness.castFromHand(player2, new SpiritualVisit(), "{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dreamcatcher);
        harness.assertNotInHand(player1, "Araba Mothrider");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A departed Dreamcatcher cannot be sacrificed to draw a card")
    void departedSourceDoesNotDraw() {
        Permanent dreamcatcher = addDreamcatcher();
        harness.setLibrary(player1, List.of(new ArabaMothrider()));

        harness.castFromHand(player1, new SpiritualVisit(), "{W}");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dreamcatcher));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dreamcatcher");
        harness.assertNotInHand(player1, "Araba Mothrider");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, WheelOfSunAndMoon.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("The sacrifice asks Wheel of Sun and Moon or Kalitas before Dreamcatcher leaves, then draws once")
    void deathReplacementQuestionEndsTheSacrificeStepAndTheDrawWaitsForIt() {
        Permanent dreamcatcher = addDreamcatcher();
        Card spell = new SpiritualVisit();
        setUpWheelAndKalitas();
        Card topCard = new ArabaMothrider();
        harness.setLibrary(player1, List.of(topCard));

        harness.castFromHand(player1, spell, "{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // CR 616.1: asked while Dreamcatcher is still on the battlefield, and the draw waits for the answer.
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder(
                "Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dreamcatcher);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);

        harness.handleListChoice(player1, "Put it on the bottom of its owner's library");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dreamcatcher);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dreamcatcher.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingDepartureBatch).isNull();
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(spell.getId());
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, WheelOfSunAndMoon.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Choosing Kalitas exiles the sacrificed Dreamcatcher, and it still draws a card")
    void choosingKalitasExilesTheSacrificedDreamcatcherAndDraws() {
        Permanent dreamcatcher = addDreamcatcher();
        setUpWheelAndKalitas();
        Card topCard = new ArabaMothrider();
        harness.setLibrary(player1, List.of(topCard));

        harness.castFromHand(player1, new SpiritualVisit(), "{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Kalitas, Traitor of Ghet");

        assertThat(gd.findExiledCard(dreamcatcher.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting after another player gained control of Dreamcatcher sacrifices nothing and draws nothing")
    void noSacrificeMeansNoDraw() {
        Permanent dreamcatcher = addDreamcatcher();
        harness.setLibrary(player1, List.of(new ArabaMothrider()));

        harness.castFromHand(player1, new SpiritualVisit(), "{W}");
        harness.inMutationScope(() -> {
            gd.playerBattlefields.get(player1.getId()).remove(dreamcatcher);
            gd.playerBattlefields.get(player2.getId()).add(dreamcatcher);
        });
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(dreamcatcher);
        harness.assertNotInHand(player1, "Araba Mothrider");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void setUpWheelAndKalitas() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new WheelOfSunAndMoon());
        wheel.setAttachedTo(player1.getId());
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
    }

    private Permanent addDreamcatcher() {
        return harness.addToBattlefieldAndReturn(player1, new Dreamcatcher());
    }
}
