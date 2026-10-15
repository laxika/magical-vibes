package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Recollect;
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

@CardUsed({WillOfTheJeskai.class, EdgarMarkov.class, GrizzlyBears.class, Shock.class, Recollect.class})
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
        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    void commanderAllowsBothModes() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        addCreatureReady(player1, commander);
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

    @Test
    void emptyHandCanStillBeDiscardedToDrawFive() {
        harness.setHand(player1, List.of(new WillOfTheJeskai()));
        harness.setHand(player2, List.of());
        fillLibrary(player1, 5);
        fillLibrary(player2, 5);

        castMode(0);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
    }

    @Test
    void bothModesGrantFlashbackToCardsDiscardedByFirstMode() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        addCreatureReady(player1, commander);
        Shock shock = new Shock();
        harness.setHand(player1, List.of(new WillOfTheJeskai(), shock));
        harness.setHand(player2, List.of());
        fillLibrary(player1, 5);
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of(), null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveFlashback(player1, 0, player2.getId());
        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    void legendaryCreatureThatIsNotACommanderDoesNotAllowBothModes() {
        addCreatureReady(player1, new EdgarMarkov());
        harness.setHand(player1, List.of(new WillOfTheJeskai()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondModeDoesNotGrantFlashbackToOpponentOrToItself() {
        Shock opponentShock = new Shock();
        WillOfTheJeskai spell = new WillOfTheJeskai();
        harness.setGraveyard(player2, List.of(opponentShock));
        harness.setHand(player1, List.of(spell));
        castMode(1);

        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFlashback(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(spell.getId());
    }

    @Test
    void sorceryAlreadyInGraveyardCanBeCastForItsManaCost() {
        Recollect recollect = new Recollect();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(recollect, bears));
        harness.setHand(player1, List.of(new WillOfTheJeskai()));
        castMode(1);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveFlashback(player1, 0, bears.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(recollect);
    }

    @Test
    void flashbackGrantIsLostWhenCardLeavesAndReentersGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new WillOfTheJeskai(), new Recollect()));
        castMode(1);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, shock.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(shock);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        harness.addMana(player1, ManaColor.RED, 1);
        int shockIndex = gd.playerGraveyards.get(player1.getId()).indexOf(shock);
        assertThatThrownBy(() -> harness.castFlashback(player1, shockIndex, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flashbackGrantExpiresAtEndOfTurn() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new WillOfTheJeskai()));
        castMode(1);
        harness.passUntil(player2, com.github.laxika.magicalvibes.model.TurnStep.UPKEEP);

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
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
