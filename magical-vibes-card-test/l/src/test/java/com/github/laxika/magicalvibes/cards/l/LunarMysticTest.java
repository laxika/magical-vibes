package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.p.PillarOfFlame;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LunarMystic.class, LeapOfFaith.class, MoorlandInquisitor.class, PillarOfFlame.class})
class LunarMysticTest extends BaseCardTest {

    private void seedDeck() {
        harness.setLibrary(player1, List.of(new MoorlandInquisitor()));
    }

    private List<Card> hand() {
        return gd.playerHands.get(player1.getId());
    }

    @Test
    @DisplayName("Paying {1} after casting an instant draws a card")
    void payingDrawsCard() {
        var mystic = harness.addToBattlefieldAndReturn(player1, new LunarMystic());
        seedDeck();
        harness.setHand(player1, List.of(new LeapOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, mystic.getId());
        harness.passBothPriorities();

        int handBefore = hand().size();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(hand()).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the may-pay prompt draws nothing")
    void decliningDrawsNothing() {
        var mystic = harness.addToBattlefieldAndReturn(player1, new LunarMystic());
        seedDeck();
        harness.setHand(player1, List.of(new LeapOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, mystic.getId());
        harness.passBothPriorities();

        int handBefore = hand().size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(hand()).hasSize(handBefore);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new LunarMystic());
        harness.setHand(player1, List.of(new MoorlandInquisitor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The payment choice waits for the triggered ability to resolve")
    void paymentWaitsForResolution() {
        var mystic = harness.addToBattlefieldAndReturn(player1, new LunarMystic());
        seedDeck();
        harness.setHand(player1, List.of(new LeapOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, mystic.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(hand()).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hand()).hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Accepting without enough mana does not draw")
    void insufficientManaDoesNotDraw() {
        var mystic = harness.addToBattlefieldAndReturn(player1, new LunarMystic());
        seedDeck();
        harness.setHand(player1, List.of(new LeapOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, mystic.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hand()).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger Lunar Mystic")
    void opponentInstantDoesNotTrigger() {
        var mystic = harness.addToBattlefieldAndReturn(player1, new LunarMystic());
        harness.setHand(player2, List.of(new LeapOfFaith()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castInstant(player2, 0, mystic.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A sorcery does not trigger Lunar Mystic")
    void sorceryDoesNotTrigger() {
        harness.addToBattlefield(player1, new LunarMystic());
        harness.setHand(player1, List.of(new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }
}
