package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.j.JohannsStopgap;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchiveDragon.class, JohannsStopgap.class, Gingerbrute.class})
class ArchiveDragonTest extends BaseCardTest {

    @Test
    @DisplayName("When Archive Dragon enters, its controller scries 2")
    void scriesTwoOnEnter() {
        Card topCard = new ArchiveDragon();
        Card bottomCard = new ArchiveDragon();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        castArchiveDragon();

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard, bottomCard);
    }

    @Test
    @DisplayName("Scrying 2 can reorder both cards and finish the ETB")
    void resolvesScryTwo() {
        Card topCard = new ArchiveDragon();
        Card bottomCard = new ArchiveDragon();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        castArchiveDragon();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomCard, topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Archive Dragon");
    }

    private void castArchiveDragon() {
        harness.castFromHand(player1, new ArchiveDragon(), "{4}{U}{U}");
    }

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new ArchiveDragon());
        addCreatureReady(player2, new Gingerbrute());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flyingCreatureCanBlockDragon() {
        addCreatureReady(player1, new ArchiveDragon());
        Permanent blocker = addCreatureReady(player2, new ArchiveDragon());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void scryCanPutBothCardsOnBottomBelowUnseenCards() {
        Card first = new ArchiveDragon();
        Card second = new ArchiveDragon();
        Card unseen = new ArchiveDragon();
        harness.setLibrary(player1, List.of(first, second, unseen));
        castArchiveDragon();
        resolveAllTriggers();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseen, second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scryWithOneCardLooksAtOnlyThatCard() {
        Card onlyCard = new ArchiveDragon();
        harness.setLibrary(player1, List.of(onlyCard));
        castArchiveDragon();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    void scryWithEmptyLibraryFinishesWithoutPrompt() {
        harness.setLibrary(player1, List.of());
        castArchiveDragon();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Archive Dragon");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardCountersOpponentSpellWhenTheyCannotPay() {
        castStopgapAtDragon(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Archive Dragon");
        harness.assertInGraveyard(player2, "Johann's Stopgap");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void payingWardAllowsOpponentSpellToResolve() {
        castStopgapAtDragon(player2, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertInHand(player1, "Archive Dragon");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void controllerCanTargetDragonWithoutPayingWard() {
        castStopgapAtDragon(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void castStopgapAtDragon(Player caster, int extraMana) {
        harness.addToBattlefield(player1, new ArchiveDragon());
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(caster, List.of(new ArchiveDragon()));
        harness.setHand(caster, List.of(new JohannsStopgap()));
        harness.addMana(caster, ManaColor.BLUE, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 3 + extraMana);
        harness.castSorcery(caster, 0, harness.getPermanentId(player1, "Archive Dragon"));
    }
}
