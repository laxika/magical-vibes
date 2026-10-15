package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FirebrandArcher;
import com.github.laxika.magicalvibes.cards.o.OpenFire;
import com.github.laxika.magicalvibes.cards.s.StrategicPlanning;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodwaterEntity.class, FirebrandArcher.class, OpenFire.class, StrategicPlanning.class})
class BloodwaterEntityTest extends BaseCardTest {

    private Permanent addBloodwater() {
        Permanent entity = harness.addToBattlefieldAndReturn(player1, new BloodwaterEntity());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return entity;
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
        OpenFire target = new OpenFire();
        BloodwaterEntity nextCard = new BloodwaterEntity();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(nextCard));
        castBloodwater();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities(); // resolve ETB -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target, nextCard);
        harness.assertNotInGraveyard(player1, "Open Fire");
    }

    @Test
    @DisplayName("Declining the ETB leaves the card in the graveyard")
    void etbDeclinedLeavesGraveyard() {
        OpenFire target = new OpenFire();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of());
        castBloodwater();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Open Fire");
    }

    @Test
    @DisplayName("ETB only offers instant/sorcery cards, not creatures")
    void etbDoesNotOfferCreatures() {
        OpenFire target = new OpenFire();
        harness.setGraveyard(player1, List.of(new FirebrandArcher(), target));
        harness.setLibrary(player1, List.of());
        castBloodwater();
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(target);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Open Fire");
        harness.assertInGraveyard(player1, "Firebrand Archer");
    }

    @Test
    @DisplayName("ETB may put a targeted sorcery on top of the library")
    void etbPutsSorceryOnTopOfLibrary() {
        StrategicPlanning target = new StrategicPlanning();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of());
        castBloodwater();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
        harness.assertNotInGraveyard(player1, "Strategic Planning");
    }

    @Test
    @DisplayName("ETB has no legal target when only the opponent has an instant in the graveyard")
    void opponentGraveyardCannotSupplyTarget() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new OpenFire()));
        castBloodwater();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Open Fire");
    }

    @Test
    @DisplayName("ETB cannot wait for an instant to enter an initially empty graveyard")
    void emptyGraveyardDoesNotLeaveTriggerOnStack() {
        harness.setGraveyard(player1, List.of());
        castBloodwater();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB cannot replace a target that leaves the graveyard")
    void removedTargetCannotBeReplaced() {
        OpenFire target = new OpenFire();
        StrategicPlanning otherCard = new StrategicPlanning();
        harness.setGraveyard(player1, List.of(target, otherCard));
        harness.setLibrary(player1, List.of());
        castBloodwater();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of(otherCard));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Strategic Planning");
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentSpellDoesNotPump() {
        Permanent entity = addBloodwater();
        harness.setHand(player2, List.of(new OpenFire()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entity)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prowess: casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent entity = addBloodwater();

        harness.setHand(player1, List.of(new OpenFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player2.getId());

        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        harness.passBothPriorities(); // resolve prowess trigger
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);
        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entity)).isEqualTo(3);
        harness.passBothPriorities(); // resolve Open Fire

        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entity)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prowess: casting a creature spell does not pump")
    void creatureSpellDoesNotPump() {
        Permanent entity = addBloodwater();

        harness.setHand(player1, List.of(new FirebrandArcher()));
        harness.addMana(player1, ManaColor.RED, 2);
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

        harness.setHand(player1, List.of(new OpenFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(3);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entity)).isEqualTo(2);
    }
}
