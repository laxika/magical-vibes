package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MACH1SwoopingScoundrel.class, AngelOfMercy.class, GrizzlyBears.class})
class MACH1SwoopingScoundrelTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 when it enters")
    void surveilsWhenItEnters() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new MACH1SwoopingScoundrel()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Surveils 1 on the first life gain each turn")
    void surveilsOnFirstLifeGainEachTurn() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new MACH1SwoopingScoundrel());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("The enter-the-battlefield trigger uses the once-each-turn limit")
    void enterTriggerUsesOnceEachTurnLimit() {
        Card etbTopCard = new GrizzlyBears();
        Card lifeGainTopCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(etbTopCard, lifeGainTopCard));
        harness.setHand(player1, List.of(new MACH1SwoopingScoundrel(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).contains(lifeGainTopCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Keeping the surveilled card still uses the shared once-each-turn limit")
    void keepingTopCardUsesOnceEachTurnLimit() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new MACH1SwoopingScoundrel(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A second life gain in the same turn does not surveil again")
    void secondLifeGainDoesNotSurveilAgain() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.addToBattlefield(player1, new MACH1SwoopingScoundrel());
        harness.setHand(player1, List.of(new AngelOfMercy(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 26);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCard).doesNotContain(secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent gaining life does not trigger surveil")
    void opponentLifeGainDoesNotTrigger() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new MACH1SwoopingScoundrel());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 23);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveilling an empty library still uses the shared once-each-turn limit")
    void emptyLibraryUsesOnceEachTurnLimit() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new MACH1SwoopingScoundrel(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
