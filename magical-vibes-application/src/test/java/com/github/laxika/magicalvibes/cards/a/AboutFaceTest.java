package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GhituFireEater;
import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AboutFace.class, GhituFireEater.class, GrimMonolith.class})
class AboutFaceTest extends BaseCardTest {

    @Test
    void switchesTargetCreaturesPowerAndToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GhituFireEater());
        creature.setPowerModifier(1);
        harness.setHand(player1, List.of(new AboutFace()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void switchWearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GhituFireEater());
        creature.setPowerModifier(1);
        harness.setHand(player1, List.of(new AboutFace()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void canTargetAnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GhituFireEater());
        creature.setPowerModifier(1);
        harness.setHand(player1, List.of(new AboutFace()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GhituFireEater());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());
        harness.setHand(player1, List.of(new AboutFace()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void twoSwitchesRestoreOriginalPowerAndToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GhituFireEater());
        creature.setPowerModifier(1);
        harness.setHand(player1, List.of(new AboutFace(), new AboutFace()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void powerBoostAppliedAfterSwitchIncreasesToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GhituFireEater());
        harness.setHand(player1, List.of(new AboutFace()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        creature.setPowerModifier(1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void zeroPowerCreatureDiesWhenSwitchedToZeroToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GhituFireEater());
        creature.setPowerModifier(-2);
        harness.setHand(player1, List.of(new AboutFace()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Ghitu Fire-Eater");
        harness.assertInGraveyard(player2, "Ghitu Fire-Eater");
    }
}
