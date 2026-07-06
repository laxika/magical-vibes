package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ExhibitionTidecallerTest extends BaseCardTest {

    private void addTidecaller(Player player) {
        Permanent perm = new Permanent(new ExhibitionTidecaller());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }

    private void setDeck(Player player, int islands) {
        List<Card> cards = new ArrayList<>();
        IntStream.range(0, islands).forEach(i -> cards.add(new Island()));
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
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
}
