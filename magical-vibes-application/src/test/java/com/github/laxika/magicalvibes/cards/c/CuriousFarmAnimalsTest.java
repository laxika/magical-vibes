package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        harness.assertInGraveyard(player1, "Curious Farm Animals");
    }

    @Test
    @DisplayName("Sacrifice ability destroys an artifact")
    void sacrificeAbilityDestroysArtifact() {
        addReadyAnimals();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();

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

    private Permanent addReadyAnimals() {
        return addCreatureReady(player1, new CuriousFarmAnimals());
    }
}
