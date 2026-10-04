package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
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

@CardUsed({ElderOwynLyons.class, BraidwoodCup.class, GrizzlyBears.class, LiquimetalCoating.class,
        Murder.class, Naturalize.class})
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

        harness.castAndResolveInstant(player1, 0, elder.getId());

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

        harness.castAndResolveInstant(player2, 0, artifact.getId());

        harness.assertOnBattlefield(player1, "Braidwood Cup");
        harness.assertInGraveyard(player2, "Naturalize");
    }

    @Test
    void artifactWardAllowsOpponentSpellWhenPaid() {
        harness.addToBattlefield(player1, new ElderOwynLyons());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Braidwood Cup");
        harness.assertInGraveyard(player1, "Braidwood Cup");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void artifactWardCountersSpellWhenOpponentDeclinesPayment() {
        harness.addToBattlefield(player1, new ElderOwynLyons());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Braidwood Cup");
        harness.assertInGraveyard(player2, "Naturalize");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void ownSpellDoesNotTriggerArtifactWard() {
        harness.addToBattlefield(player1, new ElderOwynLyons());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Braidwood Cup");
        harness.assertInGraveyard(player1, "Braidwood Cup");
    }

    @Test
    void opponentsArtifactsDoNotReceiveWard() {
        harness.addToBattlefield(player1, new ElderOwynLyons());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BraidwoodCup());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertNotOnBattlefield(player2, "Braidwood Cup");
        harness.assertInGraveyard(player2, "Braidwood Cup");
    }

    @Test
    void enteringWithoutEligibleArtifactDoesNotRequestTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new BraidwoodCup()));
        harness.setHand(player1, List.of(new ElderOwynLyons()));
        addElderMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elder Owyn Lyons");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Braidwood Cup");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void elderReceivesItsOwnWardWhenItBecomesAnArtifact() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new ElderOwynLyons());
        harness.addToBattlefield(player1, new LiquimetalCoating());
        harness.activateAbility(player1, 1, null, elder.getId());
        harness.passBothPriorities();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, elder.getId());

        harness.assertOnBattlefield(player1, "Elder Owyn Lyons");
        harness.assertInGraveyard(player2, "Murder");
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
