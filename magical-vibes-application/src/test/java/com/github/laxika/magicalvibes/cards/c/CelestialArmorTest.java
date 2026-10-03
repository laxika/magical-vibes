package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CelestialArmor.class, GrizzlyBears.class, Disenchant.class})
class CelestialArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Celestial Armor attaches it and grants temporary protection")
    void enteringAttachesAndGrantsTemporaryProtection() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CelestialArmor()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0, bears.getId());
        resolveAllTriggers();

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
        resolveAllTriggers();
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

    @Test
    @DisplayName("Flash allows casting Celestial Armor during an opponent's combat")
    void canCastDuringOpponentsCombat() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CelestialArmor()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Celestial Armor").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Celestial Armor can enter unattached when there are no creatures")
    void canCastWithoutCreatures() {
        harness.setHand(player1, List.of(new CelestialArmor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Celestial Armor").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The ETB grants protection even if Celestial Armor leaves before it resolves")
    void grantsProtectionWhenEquipmentLeavesBeforeTriggerResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CelestialArmor()));
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        Permanent armor = findPermanent(player1, "Celestial Armor");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.castInstant(player2, 0, armor.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Celestial Armor")).isZero();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Re-equipping moves the static bonuses but leaves ETB protection on the original creature")
    void reequippingDoesNotMoveTemporaryProtection() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CelestialArmor()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castArtifact(player1, 0, firstBear.getId());
        resolveAllTriggers();
        Permanent armor = findPermanent(player1, "Celestial Armor");

        harness.activateAbility(player1, 2, null, secondBear.getId());
        resolveAllTriggers();

        assertThat(armor.getAttachedTo()).isEqualTo(secondBear.getId());
        assertThat(gqs.getEffectivePower(gd, firstBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, secondBear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, secondBear, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondBear, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondBear, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Flash does not allow equipping during combat")
    void cannotEquipDuringCombat() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CelestialArmor());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Celestial Armor").getAttachedTo()).isNull();
    }
}
