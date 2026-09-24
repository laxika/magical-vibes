package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CranialPlating.class, DrossCrocodile.class})
class CranialPlatingTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 for each artifact its controller controls")
    void boostCountsArtifactsControlledByEquipmentController() {
        Permanent creature = addCreatureReady(player2, new DrossCrocodile());
        Permanent plating = harness.addToBattlefieldAndReturn(player1, new CranialPlating());
        plating.setAttachedTo(creature.getId());

        Permanent extraArtifact = harness.addToBattlefieldAndReturn(player1, new CranialPlating());
        harness.addToBattlefield(player2, new CranialPlating());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(extraArtifact);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Unattached Cranial Plating does not boost creatures")
    void unattachedPlatingDoesNotBoost() {
        Permanent creature = addCreatureReady(player1, new DrossCrocodile());
        harness.addToBattlefieldAndReturn(player1, new CranialPlating());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cranial Plating boosts only its equipped creature")
    void boostsOnlyEquippedCreature() {
        Permanent equippedCreature = addCreatureReady(player1, new DrossCrocodile());
        Permanent otherCreature = addCreatureReady(player1, new DrossCrocodile());
        Permanent plating = harness.addToBattlefieldAndReturn(player1, new CranialPlating());
        plating.setAttachedTo(equippedCreature.getId());

        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Black ability attaches Cranial Plating at instant speed")
    void blackAbilityAttachesAtInstantSpeed() {
        Permanent plating = harness.addToBattlefieldAndReturn(player1, new CranialPlating());
        Permanent creature = addCreatureReady(player1, new DrossCrocodile());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(plating.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(plating.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Equip ability attaches Cranial Plating for one mana")
    void equipAbilityAttachesForOneMana() {
        Permanent plating = harness.addToBattlefieldAndReturn(player1, new CranialPlating());
        Permanent creature = addCreatureReady(player1, new DrossCrocodile());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(plating.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Black ability cannot target a creature controlled by an opponent")
    void blackAbilityRequiresCreatureYouControl() {
        harness.addToBattlefieldAndReturn(player1, new CranialPlating());
        Permanent opponentCreature = addCreatureReady(player2, new DrossCrocodile());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Equip ability cannot target a creature controlled by an opponent")
    void equipAbilityRequiresCreatureYouControl() {
        harness.addToBattlefieldAndReturn(player1, new CranialPlating());
        Permanent opponentCreature = addCreatureReady(player2, new DrossCrocodile());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Equip ability is restricted to sorcery speed")
    void equipAbilityRequiresSorcerySpeed() {
        harness.addToBattlefieldAndReturn(player1, new CranialPlating());
        Permanent creature = addCreatureReady(player1, new DrossCrocodile());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Black ability fizzles if its target leaves before resolution")
    void blackAbilityFizzlesIfTargetLeavesBeforeResolution() {
        Permanent plating = harness.addToBattlefieldAndReturn(player1, new CranialPlating());
        Permanent creature = addCreatureReady(player1, new DrossCrocodile());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(plating.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
