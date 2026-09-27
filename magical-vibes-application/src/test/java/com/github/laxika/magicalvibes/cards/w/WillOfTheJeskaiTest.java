package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillOfTheJeskai.class, EdgarMarkov.class, GrizzlyBears.class, Shock.class})
class WillOfTheJeskaiTest extends BaseCardTest {

    @Test
    void eachPlayerMayDiscardTheirHandAndDrawFive() {
        Card player1HandCard = new GrizzlyBears();
        Card player2HandCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new WillOfTheJeskai(), player1HandCard));
        harness.setHand(player2, List.of(player2HandCard));
        fillLibrary(player1, 5);
        fillLibrary(player2, 5);

        castMode(0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1HandCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2HandCard);
    }

    @Test
    void secondModeGrantsFlashbackToInstantAndSorceryCardsInGraveyard() {
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shock, bears));
        harness.setHand(player1, List.of(new WillOfTheJeskai()));

        castMode(1);

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(shock.getId()).doesNotContain(bears.getId());
    }

    @Test
    void grantedFlashbackUsesManaCostAndExilesTheCardAfterResolution() {
        Shock shock = new Shock();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shock));

        harness.setHand(player1, List.of(new WillOfTheJeskai()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(), null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    void commanderAllowsBothModes() {
        gd.playerCommandZones.get(player1.getId()).add(new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new WillOfTheJeskai()));
        fillLibrary(player1, 5);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        fillLibrary(player2, 5);
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of(), null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(shock.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
    }

    @Test
    void cannotChooseBothModesWithoutCommander() {
        harness.setHand(player1, List.of(new WillOfTheJeskai()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMode(int mode) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{mode}, List.of(), null);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void fillLibrary(com.github.laxika.magicalvibes.model.Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        harness.setLibrary(player, cards);
    }
}
