package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SunscorchedDesert;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FailedFording.class, GrizzlyBears.class, SunscorchedDesert.class})
class FailedFordingTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target nonland permanent to its owner's hand")
    void returnsTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new FailedFording()));
        addManaForFailedFording();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Surveils 1 when you control a Desert")
    void surveilsWithDesert() {
        harness.addToBattlefield(player1, new SunscorchedDesert());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new FailedFording()));
        addManaForFailedFording();

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Does not surveil without controlling a Desert")
    void doesNotSurveilWithoutDesert() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new FailedFording()));
        addManaForFailedFording();

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SunscorchedDesert());
        UUID targetId = harness.getPermanentId(player2, "Sunscorched Desert");
        harness.setHand(player1, List.of(new FailedFording()));
        addManaForFailedFording();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Can keep the surveilled card on top")
    void keepsSurveilledCardOnTop() {
        harness.addToBattlefield(player1, new SunscorchedDesert());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new FailedFording()));
        addManaForFailedFording();

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("An opponent's Desert does not enable surveil")
    void opponentsDesertDoesNotEnableSurveil() {
        harness.addToBattlefield(player2, new SunscorchedDesert());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new FailedFording()));
        addManaForFailedFording();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Does not surveil when the only target leaves before resolution")
    void illegalTargetPreventsSurveil() {
        harness.addToBattlefield(player1, new SunscorchedDesert());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new FailedFording()));
        addManaForFailedFording();

        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Failed Fording");
    }
    private void addManaForFailedFording() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
