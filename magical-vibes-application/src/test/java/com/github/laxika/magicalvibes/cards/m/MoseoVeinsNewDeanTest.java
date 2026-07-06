package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoseoVeinsNewDeanTest extends BaseCardTest {

    // ===== ETB: create the Pest token =====

    @Test
    @DisplayName("Casting Moseo creates a 1/1 black-green Pest token on the battlefield")
    void etbCreatesPestToken() {
        harness.setHand(player1, List.of(new MoseoVeinsNewDean()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent pest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Pest"))
                .findFirst().orElse(null);
        assertThat(pest).isNotNull();
        assertThat(pest.getCard().getPower()).isEqualTo(1);
        assertThat(pest.getCard().getToughness()).isEqualTo(1);
    }

    // ===== Infusion end-step: graveyard reanimation =====

    @Test
    @DisplayName("If you gained life, returns a creature with MV <= life gained from your graveyard")
    void returnsCreatureWhenLifeGainedCoversManaValue() {
        harness.addToBattlefield(player1, new MoseoVeinsNewDean());
        GrizzlyBears bears = new GrizzlyBears(); // mana value 2
        harness.setGraveyard(player1, List.of(bears));
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);

        // End-step trigger prompts for a graveyard target
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Moseo, Vein's New Dean"));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature with mana value greater than life gained is not a valid target")
    void creatureAboveLifeGainedNotValid() {
        harness.addToBattlefield(player1, new MoseoVeinsNewDean());
        harness.setGraveyard(player1, List.of(new GrizzlyBears())); // mana value 2
        gd.lifeGainedThisTurn.put(player1.getId(), 1); // only 1 life gained

        advanceToEndStep(player1);

        // No creature with MV <= 1 → no graveyard choice prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("No trigger fires when you have not gained life this turn")
    void noTriggerWithoutLifeGain() {
        harness.addToBattlefield(player1, new MoseoVeinsNewDean());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        // lifeGainedThisTurn left at 0

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("'Up to one' — declining the target leaves the creature in the graveyard")
    void upToOneAllowsChoosingNone() {
        harness.addToBattlefield(player1, new MoseoVeinsNewDean());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        // Choose no target ("up to one")
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Helpers =====

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance POSTCOMBAT_MAIN → END_STEP, triggers fire
    }
}
