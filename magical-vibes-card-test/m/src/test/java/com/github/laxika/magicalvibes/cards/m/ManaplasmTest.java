package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AdNauseam;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.s.SigilOfDistinction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Manaplasm.class, CylianElf.class, AdNauseam.class, SigilOfDistinction.class})
class ManaplasmTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell gives +X/+X equal to that spell's mana value")
    void castingSpellBoostsByManaValue() {
        harness.addToBattlefield(player1, new Manaplasm());
        harness.setHand(player1, List.of(new CylianElf())); // mana value 2
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the boost trigger

        Permanent manaplasm = getManaplasm();
        assertThat(manaplasm.getPowerModifier()).isEqualTo(2);
        assertThat(manaplasm.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost scales with the cast spell's mana value")
    void boostScalesWithManaValue() {
        harness.addToBattlefield(player1, new Manaplasm());
        harness.setHand(player1, List.of(new AdNauseam())); // mana value 5
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities(); // resolve the boost trigger

        Permanent manaplasm = getManaplasm();
        assertThat(manaplasm.getPowerModifier()).isEqualTo(5);
        assertThat(manaplasm.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Manaplasm());
        harness.setHand(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent manaplasm = getManaplasm();
        assertThat(manaplasm.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(manaplasm.getPowerModifier()).isEqualTo(0);
        assertThat(manaplasm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The chosen X contributes to the cast spell's mana value")
    void chosenXContributesToBoost() {
        harness.addToBattlefield(player1, new Manaplasm());
        harness.setHand(player1, List.of(new SigilOfDistinction()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(getManaplasm().getPowerModifier()).isEqualTo(4);
        assertThat(getManaplasm().getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting an X spell with X zero produces no boost")
    void zeroManaValueProducesNoBoost() {
        harness.addToBattlefield(player1, new Manaplasm());
        harness.setHand(player1, List.of(new SigilOfDistinction()));

        harness.castArtifact(player1, 0, 0);
        resolveAllTriggers();

        assertThat(getManaplasm().getPowerModifier()).isZero();
        assertThat(getManaplasm().getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player1, "Sigil of Distinction");
    }

    @Test
    @DisplayName("An opponent's spell does not boost Manaplasm")
    void opponentSpellDoesNotBoost() {
        harness.addToBattlefield(player1, new Manaplasm());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new CylianElf()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(getManaplasm().getPowerModifier()).isZero();
        assertThat(getManaplasm().getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Cylian Elf");
    }

    @Test
    @DisplayName("Boosts from multiple spells accumulate during the turn")
    void multipleSpellsAccumulateBoosts() {
        harness.addToBattlefield(player1, new Manaplasm());
        harness.setHand(player1, List.of(new CylianElf(), new CylianElf()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(getManaplasm().getPowerModifier()).isEqualTo(4);
        assertThat(getManaplasm().getToughnessModifier()).isEqualTo(4);
    }

    private Permanent getManaplasm() {
        return findPermanent(player1, "Manaplasm");
    }
}
