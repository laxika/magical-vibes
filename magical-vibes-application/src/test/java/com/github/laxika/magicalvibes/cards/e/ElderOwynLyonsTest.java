package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElderOwynLyons.class, BraidwoodCup.class, GrizzlyBears.class, Murder.class, Naturalize.class})
class ElderOwynLyonsTest extends BaseCardTest {

    @Test
    void returnsTargetArtifactFromGraveyardWhenEntering() {
        Card artifact = new BraidwoodCup();
        Card nonArtifact = new GrizzlyBears();
        Card opponentArtifact = new BraidwoodCup();
        harness.setGraveyard(player1, List.of(artifact, nonArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.setHand(player1, List.of(new ElderOwynLyons()));
        addElderMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Braidwood Cup");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonArtifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentArtifact);
    }

    @Test
    void returnsTargetArtifactFromGraveyardWhenDying() {
        Card artifact = new BraidwoodCup();
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new ElderOwynLyons());
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, elder.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Braidwood Cup");
        harness.assertInGraveyard(player1, "Elder Owyn Lyons");
    }

    @Test
    void artifactWardCountersUnpaidOpponentSpell() {
        harness.addToBattlefield(player1, new ElderOwynLyons());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Braidwood Cup");
        harness.assertInGraveyard(player2, "Naturalize");
    }

    private void addElderMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
