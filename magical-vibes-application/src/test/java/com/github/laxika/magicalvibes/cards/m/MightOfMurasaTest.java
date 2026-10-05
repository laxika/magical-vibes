package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GnarlidColony;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.s.SpareSupplies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MightOfMurasa.class, GnarlidColony.class, SpareSupplies.class, IntoTheRoil.class})
class MightOfMurasaTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature +3/+3 without kicker")
    void boostsTargetWithoutKicker() {
        Permanent target = addCreatureReady(player1, new GnarlidColony());

        harness.setHand(player1, List.of(new MightOfMurasa()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gives the target creature +5/+5 when kicked")
    void boostsTargetWhenKicked() {
        Permanent target = addCreatureReady(player1, new GnarlidColony());

        harness.setHand(player1, List.of(new MightOfMurasa()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
    }

    @Test
    @DisplayName("The temporary boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent target = addCreatureReady(player1, new GnarlidColony());

        harness.setHand(player1, List.of(new MightOfMurasa()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpareSupplies());
        harness.setHand(player1, List.of(new MightOfMurasa()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can boost an opponent's creature without affecting other creatures")
    void boostsOnlyTargetedOpponentCreature() {
        Permanent target = addCreatureReady(player2, new GnarlidColony());
        Permanent other = addCreatureReady(player1, new GnarlidColony());
        harness.setHand(player1, List.of(new MightOfMurasa()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("The entire kicked boost wears off at cleanup")
    void kickedBoostWearsOffAtCleanup() {
        Permanent target = addCreatureReady(player1, new GnarlidColony());
        harness.setHand(player1, List.of(new MightOfMurasa()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Kicker cannot be paid with only the base spell's mana")
    void cannotKickWithOnlyBaseMana() {
        Permanent target = addCreatureReady(player1, new GnarlidColony());
        harness.setHand(player1, List.of(new MightOfMurasa()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        harness.assertInHand(player1, "Might of Murasa");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("A kicked spell does not boost a creature that leaves and returns before resolution")
    void doesNotBoostReturnedTarget() {
        Permanent target = addCreatureReady(player1, new GnarlidColony());
        harness.setHand(player1, List.of(new MightOfMurasa()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castKickedInstant(player1, 0, target.getId());

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInHand(player1, "Gnarlid Colony");
        harness.assertNotOnBattlefield(player1, "Gnarlid Colony");
        Permanent returned = addCreatureReady(player1, gd.playerHands.get(player1.getId()).removeFirst());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Might of Murasa");
    }
}
