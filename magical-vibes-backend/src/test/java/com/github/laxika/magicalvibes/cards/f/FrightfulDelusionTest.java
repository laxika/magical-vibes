package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDiscardsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FrightfulDelusionTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Frightful Delusion has correct effects")
    void hasCorrectEffects() {
        FrightfulDelusion card = new FrightfulDelusion();

        assertThat(EffectResolution.needsSpellTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isNull();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(TargetSpellControllerDiscardsEffect.class);
        assertThat(((TargetSpellControllerDiscardsEffect) card.getEffects(EffectSlot.SPELL).get(0)).amount()).isEqualTo(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(CounterUnlessPaysEffect.class);
        assertThat(((CounterUnlessPaysEffect) card.getEffects(EffectSlot.SPELL).get(1)).amount()).isEqualTo(1);
    }

    // ===== Opponent cannot pay — spell is countered, opponent discards =====

    @Test
    @DisplayName("Counters spell and opponent discards when they cannot pay {1}")
    void countersAndDiscardsWhenCannotPay() {
        GrizzlyBears bears = new GrizzlyBears();
        GrizzlyBears bearsInHand = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(bears, bearsInHand)));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FrightfulDelusion()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        // Discard effect resolves first — opponent must discard
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        // Spell is countered (opponent had no mana left to pay {1})
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Opponent can pay and pays — spell not countered, opponent discards =====

    @Test
    @DisplayName("Opponent discards and spell is not countered when they pay {1}")
    void discardsAndNotCounteredWhenPays() {
        LlanowarElves elves = new LlanowarElves();
        GrizzlyBears bearsInHand = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(elves, bearsInHand)));
        harness.addMana(player1, ManaColor.GREEN, 2); // 1 to cast, 1 to pay

        harness.setHand(player2, List.of(new FrightfulDelusion()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        // Discard effect resolves first
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);

        harness.handleCardChosen(player1, 0);

        // Counter-unless-pay: opponent can pay {1} — may ability choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Opponent pays
        harness.handleMayAbilityChosen(player1, true);

        // Elves should not be countered
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Llanowar Elves"));

        // Resolve the elves spell
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    // ===== Opponent can pay but declines — spell countered, opponent discards =====

    @Test
    @DisplayName("Opponent discards and spell is countered when they decline to pay")
    void discardsAndCounteredWhenDeclines() {
        LlanowarElves elves = new LlanowarElves();
        GrizzlyBears bearsInHand = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(elves, bearsInHand)));
        harness.addMana(player1, ManaColor.GREEN, 2); // 1 to cast, 1 available

        harness.setHand(player2, List.of(new FrightfulDelusion()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        // Discard first
        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);

        // Decline to pay
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    // ===== Opponent has no cards in hand — discard does nothing, spell still countered =====

    @Test
    @DisplayName("Spell is countered even when opponent has no cards to discard")
    void countersWhenOpponentHasEmptyHand() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, new ArrayList<>(List.of(elves)));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new FrightfulDelusion()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        // Opponent has no cards in hand (elves was cast), no mana to pay
        // Both effects resolve without interaction
        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack — no discard")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        GrizzlyBears bearsInHand = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(bears, bearsInHand)));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new FrightfulDelusion()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        // Remove target from stack before Frightful Delusion resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // No discard happened
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== Frightful Delusion goes to graveyard =====

    @Test
    @DisplayName("Frightful Delusion goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, new ArrayList<>(List.of(elves)));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new FrightfulDelusion()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Frightful Delusion"));
        assertThat(gd.stack).isEmpty();
    }
}
