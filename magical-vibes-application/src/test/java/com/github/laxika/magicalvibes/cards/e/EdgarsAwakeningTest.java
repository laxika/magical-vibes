package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EdgarsAwakening.class, GrizzlyBears.class, HolyDay.class, Sift.class, TormentingVoice.class})
class EdgarsAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature from graveyard to the battlefield")
    void reanimatesCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new EdgarsAwakening()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()))
                .anyMatch(c -> c.getName().equals("Edgar's Awakening"));
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreature() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new EdgarsAwakening()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discarding and paying {B} returns a creature card from graveyard to hand")
    void discardPayReturnsCreatureToHand() {
        Card creature = new GrizzlyBears();
        EdgarsAwakening awakening = new EdgarsAwakening();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new TormentingVoice(), awakening));
        harness.setLibrary(player1, List.of(new HolyDay(), new HolyDay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1); // Tormenting Voice
        harness.addMana(player1, ManaColor.BLACK, 1); // may-pay {B}
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorceryWithDiscard(player1, 0, 1);
        // Discard trigger is on the stack (possibly under Tormenting Voice) — resolve until may-pay
        while (gd.interaction.activeInteraction() == null && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{B}");

        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty() || gd.interaction.isAwaitingInput()) {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player1, false);
                continue;
            }
            if (gd.interaction.isAwaitingInput()) {
                break;
            }
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()))
                .anyMatch(c -> c.getId().equals(awakening.getId()));
    }

    @Test
    @DisplayName("Declining the discard may-pay leaves the creature in the graveyard")
    void declineLeavesCreatureInGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new TormentingVoice(), new EdgarsAwakening()));
        harness.setLibrary(player1, List.of(new HolyDay(), new HolyDay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorceryWithDiscard(player1, 0, 1);
        while (gd.interaction.activeInteraction() == null && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Self-discard via Sift also triggers the may-pay ability")
    void selfDiscardViaSiftTriggers() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new HolyDay(), new HolyDay(), new HolyDay()));
        harness.setHand(player1, List.of(new Sift(), new EdgarsAwakening()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve Sift → draw 3, discard choice

        harness.handleCardChosen(player1, 0); // discard Edgar's Awakening (index 0 after draws)

        while (gd.interaction.activeInteraction() == null && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot reanimate a creature from an opponent's graveyard")
    void cannotTargetOpponentsCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new EdgarsAwakening()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Paying for the discard trigger leaves a separate return trigger to respond to")
    void paymentCreatesRespondableReturnTrigger() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new HolyDay(), new HolyDay(), new HolyDay()));
        harness.setHand(player1, List.of(new Sift(), new EdgarsAwakening()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

            harness.handleMayAbilityChosen(player1, true);

            harness.assertInGraveyard(player1, "Grizzly Bears");
            harness.assertNotInHand(player1, "Grizzly Bears");
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            harness.assertInHand(player1, "Grizzly Bears");
            harness.assertNotInGraveyard(player1, "Grizzly Bears");
        });
    }

    @Test
    @DisplayName("Discard payment cannot return a creature without black mana")
    void cannotPayWithoutBlackMana() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new TormentingVoice(), new EdgarsAwakening()));
        harness.setLibrary(player1, List.of(new HolyDay(), new HolyDay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorceryWithDiscard(player1, 0, 1);
        while (gd.interaction.activeInteraction() == null && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("After payment, the controller chooses just one of multiple creature cards")
    void choosesOneCreatureAfterPayment() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second, new HolyDay()));
        harness.setLibrary(player1, List.of(new HolyDay(), new HolyDay(), new HolyDay()));
        harness.setHand(player1, List.of(new Sift(), new EdgarsAwakening()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 1);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(second.getId()))
                .noneMatch(c -> c.getId().equals(first.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(first.getId()))
                .noneMatch(c -> c.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("A creature that leaves the graveyard before resolution is not reanimated")
    void doesNotSubstituteForMissingTarget() {
        Card target = new GrizzlyBears();
        Card other = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(new EdgarsAwakening()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(other.getId()));
        harness.assertInGraveyard(player1, "Edgar's Awakening");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
