package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SelhoffOccultistTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_DEATH and ON_ANY_CREATURE_DIES effects")
    void hasCorrectStructure() {
        SelhoffOccultist card = new SelhoffOccultist();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst())
                .isInstanceOf(MillTargetPlayerEffect.class);
        MillTargetPlayerEffect deathEffect =
                (MillTargetPlayerEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(deathEffect.count()).isEqualTo(1);

        assertThat(card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES).getFirst())
                .isInstanceOf(MillTargetPlayerEffect.class);
    }

    // ===== ON_DEATH: Selhoff Occultist itself dies =====

    @Test
    @DisplayName("When Selhoff Occultist dies, target player mills a card")
    void selfDeathMillsTargetPlayer() {
        harness.addToBattlefield(player1, new SelhoffOccultist());

        // Kill Selhoff Occultist with Lightning Bolt (3 damage needed for 2/3)
        setupPlayer2Active();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        trimDeck(player2.getId(), 10);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        UUID occultistId = harness.getPermanentId(player1, "Selhoff Occultist");
        harness.castInstant(player2, 0, occultistId);
        harness.passBothPriorities(); // Resolve Shock → Occultist dies → death trigger

        // Player1 is prompted to choose a target player
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Target player mills 1 card
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    // ===== ON_ANY_CREATURE_DIES: another creature dies =====

    @Test
    @DisplayName("When an ally creature dies, target player mills a card")
    void allyCreatureDeathMillsTargetPlayer() {
        harness.addToBattlefield(player1, new SelhoffOccultist());
        harness.addToBattlefield(player1, new GrizzlyBears());

        // Kill ally creature with Shock
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        trimDeck(player2.getId(), 10);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → Bears die → death trigger

        // Player1 is prompted to choose a target player
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Target player mills 1 card
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("When an opponent's creature dies, target player mills a card")
    void opponentCreatureDeathMillsTargetPlayer() {
        harness.addToBattlefield(player1, new SelhoffOccultist());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        trimDeck(player2.getId(), 10);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → Bears die → death trigger

        // Player1 is prompted to choose a target player
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Target player mills 1 card
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Death trigger can target the controller for mill")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player1, new SelhoffOccultist());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        trimDeck(player1.getId(), 10);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → Bears die → death trigger

        // Choose self as target
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Controller mills 1 card
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    // ===== Helpers =====

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void trimDeck(UUID playerId, int size) {
        List<Card> deck = gd.playerDecks.get(playerId);
        while (deck.size() > size) {
            deck.removeFirst();
        }
    }
}
