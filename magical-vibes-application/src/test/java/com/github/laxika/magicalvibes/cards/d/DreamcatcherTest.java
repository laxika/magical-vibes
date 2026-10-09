package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
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

    private Permanent addDreamcatcher() {
        return harness.addToBattlefieldAndReturn(player1, new Dreamcatcher());
    }
}
