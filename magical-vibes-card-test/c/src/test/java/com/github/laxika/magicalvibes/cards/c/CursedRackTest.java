package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LibraryOfLeng;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.StealArtifact;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CursedRack.class, GrizzlyBears.class, Forest.class, Mountain.class, Plains.class,
        StealArtifact.class, LibraryOfLeng.class})
class CursedRackTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing the opponent happens during entry without a targeted triggered ability")
    void opponentChosenAsRackEnters() {
        harness.setHand(player1, List.of(new CursedRack()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cursed Rack");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.CLEANUP);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("No maximum hand size overrides Cursed Rack even when Rack enters later")
    void unlimitedHandSizeOverridesRack() {
        harness.addToBattlefield(player2, new LibraryOfLeng());
        harness.addToBattlefield(player1, new CursedRack());
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.passUntil(player2, TurnStep.CLEANUP);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(8);
    }

    @Test
    @DisplayName("Chosen opponent must discard down to four during cleanup")
    void opponentDiscardsDownToFour() {
        harness.addToBattlefield(player1, new CursedRack());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        // Opponent holds six cards; max hand size is set to four, so two must go.
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Mountain()
        )));

        harness.passUntil(player2, TurnStep.CLEANUP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Chosen opponent holding exactly four cards need not discard")
    void noDiscardWhenOpponentAtFour() {
        harness.addToBattlefield(player1, new CursedRack());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Forest(), new Mountain()
        )));

        harness.passUntil(player2, TurnStep.CLEANUP);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Controller's own hand size is unaffected (still seven)")
    void controllerHandSizeUnaffected() {
        harness.addToBattlefield(player1, new CursedRack());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        // Eight cards: a normal player discards down to seven, not four.
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Plains()
        )));

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Hand-size limit is lifted when Cursed Rack leaves the battlefield")
    void handSizeRestoredWhenRemoved() {
        harness.addToBattlefield(player1, new CursedRack());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Mountain()
        )));

        // Remove Cursed Rack before cleanup — max hand size returns to seven.
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passUntil(player2, TurnStep.CLEANUP);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(6);
    }

    @Test
    void chosenOpponentRemainsAffectedAfterControlChange() {
        harness.castFromHand(player1, new CursedRack(), "{4}");
        harness.passBothPriorities();
        Permanent rack = findPermanent(player1, "Cursed Rack");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new StealArtifact()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castEnchantment(player2, 0, rack.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(rack.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(rack.getId()));
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Forest(), new Mountain(), new Plains()
        )));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }
}
