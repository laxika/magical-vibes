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

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

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

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInHand(player2, "Cinderbones");
    }

    @Test
    @DisplayName("A reanimated creature dies normally before the delayed exile")
    void stolenCreatureDiesNormally() {
        Card target = new Cinderbones();
        harness.setGraveyard(player2, List.of(target));
        castClique();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Cinderbones"));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Cinderbones");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Persist during an opponent's turn reanimates a creature until your next end step")
    void opponentEndStepDoesNotExileReanimatedCreature() {
        Card target = new Cinderbones();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player1, new PuppeteerClique());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Puppeteer Clique"));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Cinderbones");

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Cinderbones");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Reanimation after the end step begins retains haste through cleanup")
    void hastePersistsBeyondCleanup() {
        Card target = new Cinderbones();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player1, new PuppeteerClique());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Puppeteer Clique"));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.UPKEEP);

        Permanent stolen = findPermanent(player1, "Cinderbones");
        assertThat(gqs.hasKeyword(gd, stolen, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Persist does not return a Clique that dies with a -1/-1 counter")
    void persistDoesNotReturnCliqueWithMinusCounter() {
        harness.addToBattlefield(player1, new PuppeteerClique());
        findPermanent(player1, "Puppeteer Clique").setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Puppeteer Clique"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Puppeteer Clique");
        harness.assertInGraveyard(player1, "Puppeteer Clique");
    }

    @Test
    @DisplayName("A reanimated opposing Clique returns to its owner through persist")
    void stolenCliquePersistsUnderOwnersControl() {
        Card target = new PuppeteerClique();
        harness.setGraveyard(player2, List.of(target));
        castClique();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        Permanent stolen = findPermanents(player1, "Puppeteer Clique").stream()
                .filter(p -> p.getCard().getId().equals(target.getId())).findFirst().orElseThrow();
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, stolen.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player2, "Puppeteer Clique");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Puppeteer Clique")).isEqualTo(1);
    }
}
