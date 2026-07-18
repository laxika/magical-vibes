package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PuppeteerCliqueTest extends BaseCardTest {

    private void castClique() {
        harness.setHand(player1, List.of(new PuppeteerClique()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice
    }

    // ===== ETB targeting =====

    @Test
    @DisplayName("ETB with a creature in an opponent's graveyard prompts a graveyard choice")
    void etbPromptsGraveyardChoice() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        castClique();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("ETB only offers creature cards from opponents' graveyards")
    void etbOnlyOffersOpponentCreatures() {
        Card oppCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(oppCreature, new Island(), new Pacifism()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castClique();

        List<UUID> validIds = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(validIds).containsExactly(oppCreature.getId());
    }

    @Test
    @DisplayName("ETB with no valid target does not prompt and the Clique still enters")
    void etbNoValidTargetDoesNotPrompt() {
        harness.setGraveyard(player2, List.of(new Island()));
        castClique();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Puppeteer Clique"));
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving puts the opponent's creature onto the battlefield with haste under your control")
    void resolvesAndPutsCreatureOnBattlefield() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        castClique();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent stolen = findCreatureOnBattlefield(player1.getId(), "Grizzly Bears");
        assertThat(stolen.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(stolen.isExileIfLeavesBattlefield()).isTrue();
        assertThat(gd.stolenCreatures).containsKey(stolen.getId());
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(stolen.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The stolen creature is exiled to its owner at the next end step")
    void stolenCreatureExiledAtEndStep() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        castClique();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    private Permanent findCreatureOnBattlefield(UUID playerId, String cardName) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(cardName + " not found on battlefield"));
    }
}
