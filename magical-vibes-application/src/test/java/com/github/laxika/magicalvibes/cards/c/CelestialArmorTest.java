package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CelestialArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Celestial Armor attaches it and grants temporary protection")
    void enteringAttachesAndGrantsTemporaryProtection() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CelestialArmor()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent armor = findPermanent(player1, "Celestial Armor");
        assertThat(armor.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB protection expires while the equipped bonus remains")
    void etbProtectionExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CelestialArmor()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip attaches Celestial Armor to another creature you control")
    void equipAttachesToAnotherCreature() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new CelestialArmor());
        armor.setAttachedTo(firstBear.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, secondBear.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(secondBear.getId());
        assertThat(gqs.getEffectivePower(gd, firstBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondBear)).isEqualTo(4);
    }

    @Test
    @DisplayName("Celestial Armor cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CelestialArmor()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID opponentBearId = opponentBear.getId();
        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentBearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
}
