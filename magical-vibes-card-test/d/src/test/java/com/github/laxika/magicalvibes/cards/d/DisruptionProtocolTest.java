package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinElectromancer;
import com.github.laxika.magicalvibes.cards.i.IronApprentice;
import com.github.laxika.magicalvibes.cards.s.ScrabblingClaws;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisruptionProtocol.class, GrizzlyBears.class, ScrabblingClaws.class,
        GoblinElectromancer.class, IronApprentice.class})
class DisruptionProtocolTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped artifact instead of paying {1}")
    void tapsArtifactAsAdditionalCost() {
        GrizzlyBears bears = castTargetSpell();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ScrabblingClaws());
        harness.setHand(player2, List.of(new DisruptionProtocol()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstantWithSacrifices(player2, 0, bears.getId(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Pays {1} instead of tapping an artifact")
    void paysManaAsAdditionalCost() {
        GrizzlyBears bears = castTargetSpell();
        harness.setHand(player2, List.of(new DisruptionProtocol()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, bears.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rejects a nonartifact tap choice")
    void rejectsNonartifactTapChoice() {
        GrizzlyBears bears = castTargetSpell();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new DisruptionProtocol()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(
                player2, 0, bears.getId(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tap cost");
    }

    @Test
    @DisplayName("Cannot cast without an untapped artifact or the additional mana")
    void requiresOnePaymentOption() {
        GrizzlyBears bears = castTargetSpell();
        harness.setHand(player2, List.of(new DisruptionProtocol()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pay {1}");
    }

    private GrizzlyBears castTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        return bears;
    }

    @Test
    void rejectsAlreadyTappedArtifact() {
        GrizzlyBears bears = castTargetSpell();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ScrabblingClaws());
        artifact.tap();
        harness.setHand(player2, List.of(new DisruptionProtocol()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(
                player2, 0, bears.getId(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Disruption Protocol");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    void rejectsOpponentsArtifact() {
        GrizzlyBears bears = castTargetSpell();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ScrabblingClaws());
        harness.setHand(player2, List.of(new DisruptionProtocol()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(
                player2, 0, bears.getId(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player2, "Disruption Protocol");
    }

    @Test
    void rejectsTappingMoreThanOneArtifact() {
        GrizzlyBears bears = castTargetSpell();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ScrabblingClaws());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ScrabblingClaws());
        harness.setHand(player2, List.of(new DisruptionProtocol()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(
                player2, 0, bears.getId(), List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        harness.assertInHand(player2, "Disruption Protocol");
    }

    @Test
    void canChooseManaEvenWithUntappedArtifactAvailable() {
        GrizzlyBears bears = castTargetSpell();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ScrabblingClaws());
        harness.setHand(player2, List.of(new DisruptionProtocol()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, bears.getId());

        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Scrabbling Claws");
    }

    @Test
    void costReductionAppliesToAdditionalManaOption() {
        GrizzlyBears bears = castTargetSpell();
        harness.addToBattlefield(player2, new GoblinElectromancer());
        harness.setHand(player2, List.of(new DisruptionProtocol()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, bears.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed({DisruptionProtocol.class, IronApprentice.class})
    void canTapSummoningSickArtifactCreature() {
        IronApprentice target = new IronApprentice();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        Permanent artifact = harness.enterBattlefieldAndReturn(player2, new IronApprentice());
        artifact.setSummoningSick(true);
        harness.setHand(player2, List.of(new DisruptionProtocol()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstantWithSacrifices(player2, 0, target.getId(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Iron Apprentice");
        harness.assertOnBattlefield(player2, "Iron Apprentice");
    }
}
