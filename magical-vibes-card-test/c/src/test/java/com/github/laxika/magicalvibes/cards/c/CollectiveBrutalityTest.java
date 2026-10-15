package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CollectiveBrutality.class, GrizzlyBears.class, Peek.class, Shock.class, FieryTemper.class})
class CollectiveBrutalityTest extends BaseCardTest {

    // Modes: 0 = discard I/S from hand, 1 = creature -2/-2, 2 = drain 2

    @Test
    @DisplayName("Creature mode: target gets -2/-2 until end of turn")
    void creatureModeGivesMinusTwoMinusTwo() {
        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(4);
        bigBear.setToughness(4);
        Permanent bears = addCreatureReady(player2, bigBear);
        harness.setHand(player1, List.of(new CollectiveBrutality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1}, List.of(bears.getId()), null);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(-2);
        assertThat(bears.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Drain mode: opponent loses 2 life and controller gains 2")
    void drainModeDrainsTwo() {
        harness.setHand(player1, List.of(new CollectiveBrutality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{2}, List.of(player2.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Hand mode: prompts to discard an instant or sorcery from opponent's hand")
    void handModeDiscardsInstantOrSorcery() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new CollectiveBrutality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0}, List.of(player2.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Peek");
        harness.assertNotInHand(player2, "Peek");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Two modes: escalate discards one card and both modes resolve")
    void twoModesEscalateAndResolve() {
        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(4);
        bigBear.setToughness(4);
        Permanent bears = addCreatureReady(player2, bigBear);
        harness.setHand(player1, List.of(new CollectiveBrutality(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        // Discard Shock (hand index 1) to escalate for the second mode
        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1, 2},
                List.of(bears.getId(), player2.getId()), List.of(1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Collective Brutality");
        assertThat(bears.getPowerModifier()).isEqualTo(-2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Two modes without escalate discard is rejected")
    void twoModesWithoutDiscardRejected() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveBrutality(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() ->
                harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1, 2},
                        List.of(bears.getId(), player2.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void allThreeModesPayTwoDiscardsAndResumeAfterHandChoice() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new CollectiveBrutality(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new CollectiveBrutality(), new Shock(), new Peek()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 1, 2},
                List.of(player2.getId(), bears.getId(), player2.getId()), List.of(1, 2));

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Peek");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.assertLife(player2, 20);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Collective Brutality");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Collective Brutality");
    }

    @Test
    void handWithNoEligibleCardStillAllowsDrainModeToResolve() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CollectiveBrutality(), new CollectiveBrutality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 2},
                List.of(player2.getId(), player2.getId()), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Collective Brutality");
    }

    @Test
    void emptyHandDoesNotPreventDrainModeResolving() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new CollectiveBrutality(), new CollectiveBrutality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 2},
                List.of(player2.getId(), player2.getId()), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Collective Brutality");
    }

    @Test
    void drainStillResolvesWhenCreatureTargetDiesInResponse() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new CollectiveBrutality(), new CollectiveBrutality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1, 2},
                List.of(bears.getId(), player2.getId()), List.of(1));
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Collective Brutality");
    }

    @Test
    void opponentModesCannotTargetController() {
        harness.setHand(player1, List.of(new CollectiveBrutality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{0}, List.of(player1.getId()), null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{2}, List.of(player1.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureModeCanTargetOwnCreatureAndExpiresAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new CollectiveBrutality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1}, List.of(bears.getId()), null);
        harness.passBothPriorities();
        assertThat(bears.getPowerModifier()).isEqualTo(-2);
        assertThat(bears.getToughnessModifier()).isEqualTo(-2);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void escalateDiscardTriggersMadnessBeforeBrutalityResolves() {
        FieryTemper temper = new FieryTemper();
        harness.setHand(player1, List.of(new CollectiveBrutality(), temper));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 2},
                List.of(player2.getId(), player2.getId()), List.of(1));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(temper.getId()));
        harness.assertNotInGraveyard(player1, "Fiery Temper");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Fiery Temper");
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Collective Brutality");
    }
}
