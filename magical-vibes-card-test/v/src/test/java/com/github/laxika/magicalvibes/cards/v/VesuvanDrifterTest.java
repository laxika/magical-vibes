package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VesuvanDrifter.class, GrizzlyBears.class, Island.class, Clone.class})
class VesuvanDrifterTest extends BaseCardTest {

    @Test
    @DisplayName("May reveal a creature on top and copy it with flying")
    void copiesTopCreatureWithFlying() {
        Permanent drifter = addDrifter();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(drifter.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(drifter.getCard().getPower()).isEqualTo(2);
        assertThat(drifter.getCard().getToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A noncreature top card is revealed but is not copied")
    void doesNotCopyNoncreatureTopCard() {
        Permanent drifter = addDrifter();
        harness.setLibrary(player1, List.of(new Island()));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(drifter.getCard().getName()).isEqualTo("Vesuvan Drifter");
        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The temporary copy ends at cleanup")
    void copyEndsAtCleanup() {
        Permanent drifter = addDrifter();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(drifter.getCard().getName()).isEqualTo("Grizzly Bears");

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(drifter.getCard().getName()).isEqualTo("Vesuvan Drifter");
        assertThat(drifter.getCard().getPower()).isEqualTo(2);
        assertThat(drifter.getCard().getToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isTrue();
    }

    @Test
    void mayDeclineToRevealCreature() {
        Permanent drifter = addDrifter();
        GrizzlyBears top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(drifter.getCard().getName()).isEqualTo("Vesuvan Drifter");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gameLogContains("reveals Grizzly Bears")).isFalse();
    }

    @Test
    void triggersDuringOpponentsCombatUsingControllersLibrary() {
        Permanent drifter = addDrifter();
        GrizzlyBears top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        harness.setLibrary(player2, List.of(new Island()));

        advanceToCombat(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(drifter.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void emptyLibraryDoesNotChangeDrifter() {
        Permanent drifter = addDrifter();
        harness.setLibrary(player1, List.of());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(drifter.getCard().getName()).isEqualTo("Vesuvan Drifter");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void topCardIsPrivateAndPermissionEndsWhileCopying() {
        addDrifter();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));

        advanceToCombat(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void copyingDrifterCopiesFlyingException() {
        Permanent drifter = addDrifter();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, drifter.getId());

        Permanent clone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof Clone)
                .findFirst().orElseThrow();
        assertThat(clone.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, clone, Keyword.FLYING)).isTrue();
    }

    private Permanent addDrifter() {
        return addCreatureReady(player1, new VesuvanDrifter());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
