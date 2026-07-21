package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodwaterEntityTest extends BaseCardTest {

    private Permanent addBloodwater() {
        harness.addToBattlefield(player1, new BloodwaterEntity());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void castBloodwater() {
        harness.setHand(player1, List.of(new BloodwaterEntity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB may put an instant from your graveyard on top of your library")
    void etbPutsInstantOnTopOfLibrary() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Shock())));
        harness.setLibrary(player1, new ArrayList<>());
        castBloodwater();
        harness.passBothPriorities(); // resolve creature -> ETB on stack
        harness.passBothPriorities(); // resolve ETB -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Shock");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Declining the ETB leaves the card in the graveyard")
    void etbDeclinedLeavesGraveyard() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Shock())));
        harness.setLibrary(player1, new ArrayList<>());
        castBloodwater();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("ETB only offers instant/sorcery cards, not creatures")
    void etbDoesNotOfferCreatures() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears(), new Shock())));
        harness.setLibrary(player1, new ArrayList<>());
        castBloodwater();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                (PendingInteraction.GraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleGraveyardCardChosen(player1, 1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Shock");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Prowess: casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent entity = addBloodwater();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        harness.passBothPriorities(); // resolve Shock
        harness.passBothPriorities(); // resolve prowess trigger

        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entity)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prowess: casting a creature spell does not pump")
    void creatureSpellDoesNotPump() {
        Permanent entity = addBloodwater();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entity)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prowess: the boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent entity = addBloodwater();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(3);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entity)).isEqualTo(2);
    }
}
