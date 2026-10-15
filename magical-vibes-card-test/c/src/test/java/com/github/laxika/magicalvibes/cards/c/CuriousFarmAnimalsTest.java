package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CuriousFarmAnimals.class, FountainOfYouth.class, AngelicChorus.class, GrizzlyBears.class})
class CuriousFarmAnimalsTest extends BaseCardTest {

    @Test
    @DisplayName("When sacrificed, gains 3 life")
    void gainsLifeWhenSacrificed() {
        addReadyAnimals();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        harness.assertInGraveyard(player1, "Curious Farm Animals");
    }

    @Test
    @DisplayName("Sacrifice ability destroys an artifact")
    void sacrificeAbilityDestroysArtifact() {
        addReadyAnimals();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Curious Farm Animals");
    }

    @Test
    @DisplayName("Sacrifice ability destroys an enchantment")
    void sacrificeAbilityDestroysEnchantment() {
        addReadyAnimals();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, enchantment.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a creature")
    void sacrificeAbilityCannotTargetCreature() {
        addReadyAnimals();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    @DisplayName("May choose no target even when an artifact is available")
    void mayChooseNoTargetWithLegalTargetAvailable() {
        addReadyAnimals();
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Curious Farm Animals");
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent animals = harness.addToBattlefieldAndReturn(player1, new CuriousFarmAnimals());
        animals.setSummoningSick(true);
        animals.tap();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        harness.assertInGraveyard(player1, "Curious Farm Animals");
    }

    @Test
    @DisplayName("Death trigger resolves before destroying its controller's artifact")
    void deathTriggerResolvesBeforeActivatedAbility() {
        addReadyAnimals();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, artifact.getId());

        harness.assertInGraveyard(player1, "Curious Farm Animals");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
        harness.assertOnBattlefield(player1, "Fountain of Youth");
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Dying in combat gains life for the creature's controller")
    void combatDeathGainsLife() {
        addCreatureReady(player2, new CuriousFarmAnimals());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Curious Farm Animals");
        harness.assertLife(player2, 13);
        harness.assertLife(player1, 10);
    }

    private Permanent addReadyAnimals() {
        return addCreatureReady(player1, new CuriousFarmAnimals());
    }
}
