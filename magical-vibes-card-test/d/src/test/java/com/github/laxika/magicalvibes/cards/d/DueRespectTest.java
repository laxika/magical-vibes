package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArchelosLagoonMystic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MasterSplicer;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DueRespect.class, Forest.class, GrizzlyBears.class, Ornithopter.class, MasterSplicer.class, ArchelosLagoonMystic.class})
class DueRespectTest extends BaseCardTest {

    

    @Test
    @DisplayName("Due Respect makes creatures enter tapped this turn")
    void creaturesEnterTappedThisTurn() {
        harness.setHand(player1, List.of(new DueRespect()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);

        // Now cast a creature - it should enter tapped
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Due Respect makes artifacts enter tapped this turn")
    void artifactsEnterTappedThisTurn() {
        harness.setHand(player1, List.of(new DueRespect()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player2, List.of(new Ornithopter()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        Permanent ornithopter = findPermanent(player2, "Ornithopter");
        assertThat(ornithopter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Due Respect makes lands enter tapped this turn")
    void landsEnterTappedThisTurn() {
        harness.setHand(player1, List.of(new DueRespect()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Due Respect draws a card")
    void drawsACard() {
        harness.setHand(player1, List.of(new DueRespect()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0);

        // Due Respect was cast from hand (hand -1) then draws a card (+1), net 0
        // But the card was removed from hand when cast, so hand should be handSizeBefore - 1 + 1
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Due Respect leaves permanents already on the battlefield untapped")
    void existingPermanentsRemainUntapped() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DueRespect()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
        assertThat(findPermanent(player2, "Forest").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Due Respect makes the caster's creature and its token enter tapped")
    void creatureAndTokenEnterTapped() {
        harness.setHand(player1, List.of(new DueRespect()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.setHand(player1, List.of(new MasterSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Master Splicer").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Phyrexian Golem").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Due Respect expires after the turn ends")
    void permanentsEnterUntappedNextTurn() {
        harness.setHand(player1, List.of(new DueRespect()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        assertThat(findPermanent(player2, "Forest").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Due Respect and untapped Archelos let the entering permanent's controller choose")
    void competingEntryReplacementsRequireControllerChoice() {
        harness.addToBattlefield(player1, new ArchelosLagoonMystic());
        harness.setHand(player1, List.of(new DueRespect()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput())
                .as("The entering permanent's controller must be offered the replacement order")
                .isTrue();
    }
}
