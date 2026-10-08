package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.r.RenegadeFreighter;
import com.github.laxika.magicalvibes.cards.w.WeldingSparks;
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

@CardUsed({VeteranMotorist.class, RenegadeFreighter.class, WeldingSparks.class})
class VeteranMotoristTest extends BaseCardTest {

    @Test
    @DisplayName("When Veteran Motorist enters, it offers scry 2")
    void etbOffersScryTwo() {
        harness.setHand(player1, List.of(new VeteranMotorist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("When Veteran Motorist crews a Vehicle, that Vehicle gets +1/+1 until end of turn")
    void vehicleGetsBoostWhenMotoristCrewsIt() {
        addCreatureReady(player1, new VeteranMotorist());
        Permanent vehicle = addCreatureReady(player1, new RenegadeFreighter());

        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(4);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(4);
    }

    @Test
    @DisplayName("The Vehicle boost expires at end of turn")
    void vehicleBoostExpiresAtEndOfTurn() {
        addCreatureReady(player1, new VeteranMotorist());
        Permanent vehicle = addCreatureReady(player1, new RenegadeFreighter());

        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(3);
    }

    @Test
    @DisplayName("Scry can keep one card on top and put the other on the bottom")
    void scryMovesChosenCardToBottom() {
        VeteranMotorist first = new VeteranMotorist();
        RenegadeFreighter second = new RenegadeFreighter();
        VeteranMotorist third = new VeteranMotorist();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new VeteranMotorist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry 2 looks at the only card in a one-card library")
    void scryWithOneCardInLibrary() {
        RenegadeFreighter remaining = new RenegadeFreighter();
        harness.setLibrary(player1, List.of(remaining));
        harness.setHand(player1, List.of(new VeteranMotorist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(remaining);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry with an empty library completes without a choice")
    void scryWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new VeteranMotorist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Veteran Motorist");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The crew bonus still resolves after Veteran Motorist dies")
    void crewBonusSurvivesSourceRemoval() {
        Permanent motorist = harness.addToBattlefieldAndReturn(player1, new VeteranMotorist());
        Permanent vehicle = addCreatureReady(player1, new RenegadeFreighter());
        harness.setHand(player2, List.of(new WeldingSparks()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.castAndResolveInstant(player2, 0, motorist.getId());
        harness.assertInGraveyard(player1, "Veteran Motorist");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(4);
    }

    @Test
    @DisplayName("Crewing an already animated Vehicle with another Motorist adds another bonus")
    void repeatedCrewingStacksBonuses() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new VeteranMotorist());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new VeteranMotorist());
        Permanent vehicle = addCreatureReady(player1, new RenegadeFreighter());

        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);

        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(5);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
