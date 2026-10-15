package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HitchclawRecluse;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.n.NullhideFerox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamEater.class, HitchclawRecluse.class, Island.class, NullhideFerox.class})
class DreamEaterTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils fewer than four cards, then may return an opposing nonland permanent")
    void surveilsAndReturnsOpponentPermanent() {
        Card topCard = new HitchclawRecluse();
        Card secondCard = new HitchclawRecluse();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.addToBattlefield(player2, new HitchclawRecluse());
        UUID targetId = harness.getPermanentId(player2, "Hitchclaw Recluse");
        castDreamEater();

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Hitchclaw Recluse");
        harness.assertInHand(player2, "Hitchclaw Recluse");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondCard);
    }

    @Test
    @DisplayName("May decline leaves the opposing permanent on the battlefield")
    void decliningBounceLeavesPermanent() {
        harness.setLibrary(player1, List.of(new HitchclawRecluse(), new HitchclawRecluse(), new HitchclawRecluse(), new HitchclawRecluse()));
        harness.addToBattlefield(player2, new HitchclawRecluse());
        UUID targetId = harness.getPermanentId(player2, "Hitchclaw Recluse");
        castDreamEater();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2, 3), List.of()));
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Hitchclaw Recluse");
    }

    @Test
    @DisplayName("The reflexive target excludes lands and permanents controlled by the caster")
    void reflexiveTargetIsOpponentNonlandPermanent() {
        harness.setLibrary(player1, List.of(new HitchclawRecluse(), new HitchclawRecluse(), new HitchclawRecluse(), new HitchclawRecluse()));
        harness.addToBattlefield(player1, new HitchclawRecluse());
        harness.addToBattlefield(player2, new HitchclawRecluse());
        harness.addToBattlefield(player2, new Island());
        UUID ownCreatureId = harness.getPermanentId(player1, "Hitchclaw Recluse");
        UUID opposingCreatureId = harness.getPermanentId(player2, "Hitchclaw Recluse");
        UUID opposingLandId = harness.getPermanentId(player2, "Island");
        castDreamEater();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2, 3), List.of()));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds())
                .contains(opposingCreatureId)
                .doesNotContain(ownCreatureId, opposingLandId);
    }

    @Test
    void emptyLibraryStillAllowsBounce() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player2, new HitchclawRecluse());
        UUID targetId = harness.getPermanentId(player2, "Hitchclaw Recluse");
        castDreamEater();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, targetId);
        harness.assertOnBattlefield(player2, "Hitchclaw Recluse");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Hitchclaw Recluse");
        harness.assertInHand(player2, "Hitchclaw Recluse");
    }

    @Test
    void surveilReordersFourCardsAndLeavesFifthUntouchedWithoutBounceTarget() {
        Card first = new HitchclawRecluse();
        Card second = new Island();
        Card third = new HitchclawRecluse();
        Card fourth = new Island();
        Card fifth = new HitchclawRecluse();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.addToBattlefield(player2, new Island());
        castDreamEater();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil.cards()).containsExactly(first, second, third, fourth);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(3, 1), List.of(0, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, second, fifth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    void reflexiveTargetExcludesOpponentHexproofPermanent() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.addToBattlefield(player2, new NullhideFerox());
        harness.addToBattlefield(player2, new HitchclawRecluse());
        UUID hexproofId = harness.getPermanentId(player2, "Nullhide Ferox");
        UUID legalId = harness.getPermanentId(player2, "Hitchclaw Recluse");
        castDreamEater();
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(legalId).doesNotContain(hexproofId);
    }

    private void castDreamEater() {
        harness.setHand(player1, List.of(new DreamEater()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
    }
}
