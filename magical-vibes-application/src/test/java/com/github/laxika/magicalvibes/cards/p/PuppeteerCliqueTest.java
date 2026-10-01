package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AphoticWisps;
import com.github.laxika.magicalvibes.cards.c.Cinderbones;
import com.github.laxika.magicalvibes.cards.c.ConsignToDream;
import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PuppeteerClique.class, Cinderbones.class, Island.class, AphoticWisps.class,
        ConsignToDream.class, FlameJavelin.class})
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
        harness.setGraveyard(player2, List.of(new Cinderbones()));
        castClique();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("ETB only offers creature cards from opponents' graveyards")
    void etbOnlyOffersOpponentCreatures() {
        Card oppCreature = new Cinderbones();
        harness.setGraveyard(player2, List.of(oppCreature, new Island(), new AphoticWisps()));
        harness.setGraveyard(player1, List.of(new Cinderbones()));
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
        harness.assertOnBattlefield(player1, "Puppeteer Clique");
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving puts the opponent's creature onto the battlefield with haste under your control")
    void resolvesAndPutsCreatureOnBattlefield() {
        Card target = new Cinderbones();
        harness.setGraveyard(player2, List.of(target));
        castClique();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent stolen = findPermanent(player1, "Cinderbones");
        assertThat(stolen.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player2, "Cinderbones");
    }

    @Test
    @DisplayName("The stolen creature is exiled to its owner at the next end step")
    void stolenCreatureExiledAtEndStep() {
        Card target = new Cinderbones();
        harness.setGraveyard(player2, List.of(target));
        castClique();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cinderbones");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cinderbones"));
    }

    @Test
    @DisplayName("Persist returns Puppeteer Clique with a -1/-1 counter")
    void persistReturnsCliqueWithMinusCounter() {
        harness.addToBattlefield(player1, new PuppeteerClique());
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Puppeteer Clique"));
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Puppeteer Clique");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A stolen creature that leaves before the next end step is not exiled")
    void stolenCreatureCanLeaveBeforeEndStep() {
        Card target = new Cinderbones();
        harness.setGraveyard(player2, List.of(target));
        castClique();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        Permanent stolen = findPermanent(player1, "Cinderbones");
        harness.setHand(player1, List.of(new ConsignToDream()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, stolen.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Cinderbones");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Cinderbones"));
        harness.assertInHand(player2, "Cinderbones");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Cinderbones");
    }
}
