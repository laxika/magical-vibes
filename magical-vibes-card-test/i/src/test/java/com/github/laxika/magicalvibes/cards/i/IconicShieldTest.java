package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AIMBot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IconicShield.class, AIMBot.class})
class IconicShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+2 without indestructible")
    void equippedCreatureGetsBoostWithoutIndestructible() {
        Permanent creature = addCreatureReady(player1, new AIMBot());
        Permanent shield = addCreatureReady(player1, new IconicShield());
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Attacking gives another attacking creature indestructible until end of turn")
    void attackingProtectsAnotherAttacker() {
        Permanent equippedCreature = addCreatureReady(player1, new AIMBot());
        Permanent otherAttacker = addCreatureReady(player1, new AIMBot());
        addCreatureReady(player1, new AIMBot());
        Permanent shield = addCreatureReady(player1, new IconicShield());
        shield.setAttachedTo(equippedCreature.getId());

        declareAttackers(List.of(0, 1, 2));

        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target the equipped creature")
    void cannotTargetEquippedCreature() {
        Permanent equippedCreature = addCreatureReady(player1, new AIMBot());
        addCreatureReady(player1, new AIMBot());
        Permanent shield = addCreatureReady(player1, new IconicShield());
        shield.setAttachedTo(equippedCreature.getId());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, equippedCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent equippedCreature = addCreatureReady(player1, new AIMBot());
        addCreatureReady(player1, new AIMBot());
        Permanent nonAttacker = addCreatureReady(player1, new AIMBot());
        Permanent shield = addCreatureReady(player1, new IconicShield());
        shield.setAttachedTo(equippedCreature.getId());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equip costs three mana and attaches on resolution")
    void equipAttachesOnResolution() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new IconicShield());
        Permanent creature = addCreatureReady(player1, new AIMBot());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(shield.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new IconicShield());
        Permanent creature = addCreatureReady(player2, new AIMBot());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The equipped creature is the source of the attack ability")
    void equippedCreatureIsAttackAbilitySource() {
        Permanent equippedCreature = addCreatureReady(player1, new AIMBot());
        Permanent otherAttacker = addCreatureReady(player1, new AIMBot());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new IconicShield());
        shield.setAttachedTo(equippedCreature.getId());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, otherAttacker.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(equippedCreature.getId());

        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The creature's controller chooses the attack ability's target")
    void creatureControllerChoosesTargetWithOpponentsEquipment() {
        Permanent equippedCreature = addCreatureReady(player2, new AIMBot());
        Permanent otherAttacker = addCreatureReady(player2, new AIMBot());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new IconicShield());
        shield.setAttachedTo(equippedCreature.getId());

        declareAttackers(player2, List.of(0, 1));

        harness.handlePermanentChosen(player2, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}