package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExhibitionTidecaller.class, GrizzlyBears.class, Hurricane.class, Island.class, Shock.class})
class ExhibitionTidecallerTest extends BaseCardTest {

    private void addTidecaller(Player player) {
        harness.addToBattlefield(player, new ExhibitionTidecaller());
    }

    private void setDeck(Player player, int islands) {
        harness.setLibrary(player, IntStream.range(0, islands).mapToObj(i -> new Island()).toList());
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Casting a cheap instant mills the target player three cards")
    void cheapSpellMillsThree() {
        addTidecaller(player1);
        setDeck(player2, 12);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve mill trigger

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Casting a five-mana spell mills the target player ten cards instead")
    void fiveManaSpellMillsTen() {
        addTidecaller(player1);
        setDeck(player2, 12);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));

        harness.castSorcery(player1, 0, 4);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve mill trigger

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A four-mana spell mills three (below threshold)")
    void fourManaSpellMillsThree() {
        addTidecaller(player1);
        setDeck(player2, 12);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));

        harness.castSorcery(player1, 0, 3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve mill trigger

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        addTidecaller(player1);
        setDeck(player2, 12);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void controllerCanTargetTheirOwnLibrary() {
        addTidecaller(player1);
        setDeck(player1, 12);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(9);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void opponentCastingInstantDoesNotTrigger() {
        addTidecaller(player1);
        setDeck(player1, 12);
        setUpMainPhase(player2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(12);
    }

    @Test
    void upgradedTriggerMillsOnlyTheCardsRemainingInLibrary() {
        addTidecaller(player1);
        setDeck(player2, 7);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.setHand(player1, List.of(new Hurricane()));

        harness.castSorcery(player1, 0, 5);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void upgradedTriggerStillResolvesAfterTidecallerDies() {
        var tidecaller = harness.addToBattlefieldAndReturn(player1, new ExhibitionTidecaller());
        setDeck(player2, 12);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));

        harness.castSorcery(player1, 0, 4);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castInstant(player2, 0, tidecaller.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tidecaller);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(11);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }
}
