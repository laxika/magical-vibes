package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RainOfRiches.class, GrizzlyBears.class, LlanowarElves.class})
class RainOfRichesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two Treasures")
    void entersWithTwoTreasures() {
        castRainOfRiches();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("The first spell using Treasure mana each turn cascades")
    void firstTreasureManaSpellCascades() {
        castRainOfRiches();
        gd.playerDecks.get(player1.getId()).clear();
        LlanowarElves cascadeHit = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).add(cascadeHit);

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent treasure = findPermanents(player1, "Treasure").getFirst();
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Llanowar Elves");
    }

    @Test
    @DisplayName("A spell without Treasure mana does not cascade")
    void spellWithoutTreasureManaDoesNotCascade() {
        castRainOfRiches();
        gd.playerDecks.get(player1.getId()).clear();
        LlanowarElves cascadeHit = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).add(cascadeHit);

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("The second Treasure-funded spell each turn does not cascade")
    void secondTreasureManaSpellDoesNotCascade() {
        castRainOfRiches();
        gd.playerDecks.get(player1.getId()).clear();
        LlanowarElves firstHit = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).add(firstHit);

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addTreasureManaAndCastFirstTreasureSpell();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        resolveAllTriggers();
        LlanowarElves untouched = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).add(untouched);

        addTreasureManaAndCastSecondTreasureSpell();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(untouched);
    }

    private void castRainOfRiches() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new RainOfRiches()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addTreasureManaAndCastFirstTreasureSpell() {
        activateTreasureForGreen();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void addTreasureManaAndCastSecondTreasureSpell() {
        activateTreasureForGreen();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private void activateTreasureForGreen() {
        Permanent treasure = findPermanents(player1, "Treasure").getFirst();
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "GREEN");
    }
}
