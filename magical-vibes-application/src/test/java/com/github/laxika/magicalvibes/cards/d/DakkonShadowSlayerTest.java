package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DakkonShadowSlayer.class, Forest.class, Island.class, Swamp.class,
        GrizzlyBears.class, SolRing.class})
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
        harness.setLibrary(player1, new ArrayList<>(List.of(top, second)));

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
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

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

    private Permanent addReadyDakkon(Player player, int loyalty) {
        Permanent dakkon = new Permanent(new DakkonShadowSlayer());
        dakkon.setCounterCount(CounterType.LOYALTY, loyalty);
        dakkon.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(dakkon);
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
