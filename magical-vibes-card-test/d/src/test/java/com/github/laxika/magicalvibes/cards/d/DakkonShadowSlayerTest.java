package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.ThoughtMonitor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DakkonShadowSlayer.class, Forest.class, Island.class, Swamp.class,
        GrizzlyBears.class, SolRing.class, OrnithopterOfParadise.class, ThoughtMonitor.class})
class DakkonShadowSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Dakkon enters with loyalty equal to lands you control")
    void entersWithLandsControlledAsLoyalty() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new DakkonShadowSlayer()));
        addDakkonMana();

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        Permanent dakkon = findPermanent(player1, "Dakkon, Shadow Slayer");
        assertThat(dakkon.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("+1 surveils two cards")
    void plusOneSurveilsTwo() {
        Permanent dakkon = addReadyDakkon(player1, 1);
        Card top = new GrizzlyBears();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(top, second));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isEqualTo(second);
        assertThat(dakkon.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 exiles a target creature")
    void minusThreeExilesCreature() {
        Permanent dakkon = addReadyDakkon(player1, 3);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(
                bears.getCard());
        assertThat(dakkon.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-3 cannot target a noncreature")
    void minusThreeCannotTargetNoncreature() {
        addReadyDakkon(player1, 3);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-6 puts an artifact from the graveyard onto the battlefield")
    void minusSixReturnsArtifactFromGraveyard() {
        addReadyDakkon(player1, 7);
        Card solRing = new SolRing();
        Card nonArtifact = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(solRing, nonArtifact));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(solRing.getId());

        harness.handleMultipleCardsChosen(player1, List.of(solRing.getId()));

        harness.assertOnBattlefield(player1, "Sol Ring");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonArtifact);
    }

    @Test
    @DisplayName("Dakkon enters with no loyalty and dies when you control no lands")
    void entersWithoutLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DakkonShadowSlayer()));
        addDakkonMana();

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dakkon, Shadow Slayer");
        harness.assertInGraveyard(player1, "Dakkon, Shadow Slayer");
    }

    @Test
    @DisplayName("-6 can put an artifact from hand without paying its mana cost")
    void minusSixPutsArtifactFromHand() {
        Permanent dakkon = addReadyDakkon(player1, 7);
        Card artifact = new OrnithopterOfParadise();
        Card otherArtifact = new OrnithopterOfParadise();
        harness.setHand(player1, List.of(artifact));
        harness.setGraveyard(player1, List.of(otherArtifact));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(artifact.getId(), otherArtifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        harness.assertOnBattlefield(player1, "Ornithopter of Paradise");
        harness.assertNotInHand(player1, "Ornithopter of Paradise");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherArtifact);
        assertThat(dakkon.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-6 allows declining even when artifacts are available in both zones")
    void minusSixMayBeDeclined() {
        addReadyDakkon(player1, 7);
        Card handArtifact = new OrnithopterOfParadise();
        Card graveyardArtifact = new OrnithopterOfParadise();
        harness.setHand(player1, List.of(handArtifact));
        harness.setGraveyard(player1, List.of(graveyardArtifact));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardArtifact);
        harness.assertNotOnBattlefield(player1, "Ornithopter of Paradise");
    }

    @Test
    @DisplayName("Artifacts put onto the battlefield by -6 trigger their enter abilities")
    void minusSixTriggersArtifactEnterAbility() {
        addReadyDakkon(player1, 7);
        Card monitor = new ThoughtMonitor();
        Card first = new OrnithopterOfParadise();
        Card second = new OrnithopterOfParadise();
        harness.setHand(player1, List.of(monitor));
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(monitor.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thought Monitor");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("+1 can keep both surveilled cards in either order")
    void plusOneReordersKeptCards() {
        addReadyDakkon(player1, 1);
        Card first = new OrnithopterOfParadise();
        Card second = new ThoughtMonitor();
        Card third = new OrnithopterOfParadise();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyDakkon(Player player, int loyalty) {
        Permanent dakkon = harness.addToBattlefieldAndReturn(player, new DakkonShadowSlayer());
        dakkon.setCounterCount(CounterType.LOYALTY, loyalty);
        dakkon.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return dakkon;
    }

    private void addDakkonMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
