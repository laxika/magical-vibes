package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AbundanceDrawReplacementEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AbundanceTest extends BaseCardTest {


    @Test
    @DisplayName("Abundance has correct card properties")
    void hasCorrectProperties() {
        Abundance card = new Abundance();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(AbundanceDrawReplacementEffect.class);
    }

    @Test
    @DisplayName("Can replace draw step draw with nonland card")
    void replacesDrawStepDrawWithNonland() {
        harness.addToBattlefield(player1, new Abundance());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island()
        )));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);

        harness.handleListChoice(player1, "NONLAND");

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Island");
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Declining Abundance uses a normal draw")
    void decliningAbundanceDrawsNormally() {
        harness.addToBattlefield(player1, new Abundance());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears()
        )));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing land can require bottom reorder for revealed nonlands")
    void choosingLandCanRequireBottomReorder() {
        harness.addToBattlefield(player1, new Abundance());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new GrizzlyBears(),
                new Peek(),
                new Forest(),
                new Island()
        )));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "LAND");

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        gs.handleLibraryCardsReordered(gd, player1, List.of(1, 0));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Island");
        assertThat(gd.playerDecks.get(player1.getId()).get(1).getName()).isEqualTo("Peek");
        assertThat(gd.playerDecks.get(player1.getId()).get(2).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Each draw from draw-two effect gets its own Abundance choice")
    void drawTwoPromptsPerDraw() {
        harness.addToBattlefield(player1, new Abundance());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island()
        )));
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        harness.handleListChoice(player1, "NONLAND");

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Island"));
    }
}
