package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({UtilityKnife.class, GrizzlyBears.class})
class UtilityKnifeTest extends BaseCardTest {

    @Test
    @DisplayName("Attachment trigger attaches to a creature you control and boosts it")
    void entersAttachedAndBoostsCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UtilityKnife()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Utility Knife").getAttachedTo()).isNull();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        Permanent equipment = findPermanent(player1, "Utility Knife");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip moves Utility Knife to another creature")
    void equipMovesEquipmentToAnotherCreature() {
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new UtilityKnife());
        equipment.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(equipment), null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature when entering")
    void cannotTargetOpponentsCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UtilityKnife()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enters unattached when its controller has no creatures")
    void entersUnattachedWithoutCreatureTarget() {
        harness.setHand(player1, List.of(new UtilityKnife()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Utility Knife").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new UtilityKnife());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Utility Knife").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated with only two mana")
    void equipRequiresThreeMana() {
        harness.addToBattlefield(player1, new UtilityKnife());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Utility Knife").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void equipRequiresMainPhase() {
        harness.addToBattlefield(player1, new UtilityKnife());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Utility Knife").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated during an opponent's turn")
    void equipRequiresControllersTurn() {
        harness.addToBattlefield(player1, new UtilityKnife());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Utility Knife").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Attachment trigger does nothing when its target leaves the battlefield")
    void attachmentTargetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UtilityKnife()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Utility Knife").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip keeps its original attachment when the new target leaves")
    void equipTargetLeavesBeforeResolution() {
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new UtilityKnife());
        equipment.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 2, null, secondCreature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(secondCreature);
        gd.playerGraveyards.get(player1.getId()).add(secondCreature.getCard());
        resolveAllTriggers();

        assertThat(equipment.getAttachedTo()).isEqualTo(firstCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(3);
    }
}
