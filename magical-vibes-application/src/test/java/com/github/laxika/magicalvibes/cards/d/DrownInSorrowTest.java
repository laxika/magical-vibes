package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.n.NyxbornEidolon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrownInSorrow.class, AvatarOfMight.class, NyxbornEidolon.class})
class DrownInSorrowTest extends BaseCardTest {

    @Test
    @DisplayName("Gives every creature -2/-2, including the caster's own")
    void weakensAllCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new AvatarOfMight());
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        castDrownInSorrow();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(6);

        keepScryCardOnTop();
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        castDrownInSorrow();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(8);
    }

    @Test
    @DisplayName("Scry 1 begins after the creatures are weakened")
    void scriesAfterWeakeningCreatures() {
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        castDrownInSorrow();

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(6);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards().get(0);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Creatures with lethal toughness reduction die only after the scry choice")
    void lethalToughnessReductionWaitsUntilResolutionFinishes() {
        NyxbornEidolon ownCard = new NyxbornEidolon();
        NyxbornEidolon opposingCard = new NyxbornEidolon();
        Permanent ownCreature = addCreatureReady(player1, ownCard);
        Permanent opposingCreature = addCreatureReady(player2, opposingCard);

        castDrownInSorrow();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(-1);

        keepScryCardOnTop();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCard);
    }

    @Test
    @DisplayName("Resolves with an empty library without waiting for a scry choice")
    void resolvesWithEmptyLibrary() {
        NyxbornEidolon creatureCard = new NyxbornEidolon();
        Permanent creature = addCreatureReady(player2, creatureCard);
        harness.setLibrary(player1, List.of());

        castDrownInSorrow();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creatureCard);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not weakened")
    void doesNotAffectCreaturesEnteringLater() {
        castDrownInSorrow();
        keepScryCardOnTop();
        harness.setHand(player1, List.of(new NyxbornEidolon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card instanceof NyxbornEidolon);
    }

    @Test
    @DisplayName("Scry 1 can keep the top card without changing the rest of either library")
    void keepsTopCardAndLeavesOpponentLibraryAlone() {
        DrownInSorrow topCard = new DrownInSorrow();
        NyxbornEidolon nextCard = new NyxbornEidolon();
        NyxbornEidolon opponentTopCard = new NyxbornEidolon();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentTopCard));

        castDrownInSorrow();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        keepScryCardOnTop();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
    }

    private void castDrownInSorrow() {
        harness.setHand(player1, List.of(new DrownInSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }

    private void keepScryCardOnTop() {
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }
}
