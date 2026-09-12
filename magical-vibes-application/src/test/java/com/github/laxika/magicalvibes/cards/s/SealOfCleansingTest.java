package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SealOfCleansing.class, BottleGnomes.class, AngelicChorus.class, GrizzlyBears.class})
class SealOfCleansingTest extends BaseCardTest {

    @Test
    @DisplayName("Ability destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player1, new SealOfCleansing());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BottleGnomes());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bottle Gnomes");
        harness.assertInGraveyard(player2, "Bottle Gnomes");
    }

    @Test
    @DisplayName("Ability destroys target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player1, new SealOfCleansing());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Seal of Cleansing is sacrificed as a cost")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new SealOfCleansing());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BottleGnomes());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Seal of Cleansing");
        harness.assertInGraveyard(player1, "Seal of Cleansing");
    }

    @Test
    @DisplayName("Ability cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new SealOfCleansing());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    @DisplayName("Ability can destroy an artifact controlled by its controller")
    void destroysOwnArtifact() {
        harness.addToBattlefield(player1, new SealOfCleansing());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bottle Gnomes");
        harness.assertInGraveyard(player1, "Bottle Gnomes");
    }

    @Test
    @DisplayName("Ability respects a regeneration shield")
    void respectsRegenerationShield() {
        harness.addToBattlefield(player1, new SealOfCleansing());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BottleGnomes());
        target.setRegenerationShield(1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Bottle Gnomes");
        harness.assertNotInGraveyard(player2, "Bottle Gnomes");
    }
}
