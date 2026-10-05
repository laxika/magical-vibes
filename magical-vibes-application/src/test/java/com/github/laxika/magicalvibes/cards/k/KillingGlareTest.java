package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.MillennialGargoyle;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KillingGlare.class, MillennialGargoyle.class, PropheticPrism.class})
class KillingGlareTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with power X or less")
    void destroysCreatureWithinPowerLimit() {
        Permanent target = addCreature(player2, 2, 2);

        castKillingGlare(2, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Millennial Gargoyle");
        harness.assertInGraveyard(player2, "Millennial Gargoyle");
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than X")
    void rejectsCreatureAbovePowerLimit() {
        Permanent target = addCreature(player2, 3, 3);

        assertThatThrownBy(() -> castKillingGlare(2, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Checks the target's power again when it resolves")
    void fizzlesIfTargetBecomesTooPowerful() {
        Permanent target = addCreature(player2, 2, 2);

        castKillingGlare(2, target);
        target.setPowerModifier(target.getPowerModifier() + 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Millennial Gargoyle");
    }

    @Test
    @DisplayName("X=0 can destroy a creature with zero power")
    void zeroXDestroysZeroPowerCreature() {
        Permanent target = addCreature(player2, 0, 4);

        castKillingGlare(0, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Millennial Gargoyle");
    }

    @Test
    @DisplayName("Destroys a creature whose power is strictly less than X")
    void destroysCreatureBelowPowerLimit() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MillennialGargoyle());

        castKillingGlare(3, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Millennial Gargoyle");
    }

    @Test
    @DisplayName("Can destroy a creature controlled by the caster")
    void destroysOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MillennialGargoyle());

        castKillingGlare(2, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Millennial Gargoyle");
        harness.assertInGraveyard(player1, "Millennial Gargoyle");
    }

    @Test
    @DisplayName("X=0 cannot target a creature with positive power")
    void zeroXRejectsPositivePower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MillennialGargoyle());

        assertThatThrownBy(() -> castKillingGlare(0, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 can destroy a creature with negative power")
    void zeroXDestroysNegativePowerCreature() {
        Permanent target = addCreature(player2, -1, 2);

        castKillingGlare(0, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Millennial Gargoyle");
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void rejectsNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());

        assertThatThrownBy(() -> castKillingGlare(5, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Target remains legal when its power decreases before resolution")
    void destroysTargetAfterPowerDecreases() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MillennialGargoyle());

        castKillingGlare(2, target);
        target.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Millennial Gargoyle");
    }

    @Test
    @DisplayName("Destruction allows regeneration")
    void regenerationSavesTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MillennialGargoyle());
        target.setRegenerationShield(1);

        castKillingGlare(2, target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Millennial Gargoyle");
        harness.assertNotInGraveyard(player2, "Millennial Gargoyle");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Indestructible prevents destruction of an otherwise legal target")
    void indestructibleSavesTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MillennialGargoyle());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castKillingGlare(2, target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Millennial Gargoyle");
        harness.assertNotInGraveyard(player2, "Millennial Gargoyle");
        harness.assertInGraveyard(player1, "Killing Glare");
    }

    private void castKillingGlare(int xValue, Permanent target) {
        harness.setHand(player1, List.of(new KillingGlare()));
        harness.addMana(player1, ManaColor.BLACK, xValue + 1);
        harness.castInstant(player1, 0, xValue, target.getId());
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player, int power, int toughness) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MillennialGargoyle());
        permanent.setPowerModifier(power - 2);
        permanent.setToughnessModifier(toughness - 2);
        return permanent;
    }
}
