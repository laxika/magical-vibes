package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlabasterHostSanctifier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirranBanesplitter.class, AlabasterHostSanctifier.class})
class MirranBanesplitterTest extends BaseCardTest {

    @Test
    @DisplayName("When Mirran Banesplitter enters, it attaches to the target creature")
    void entersAndAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new AlabasterHostSanctifier());
        harness.setHand(player1, List.of(new MirranBanesplitter()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        Permanent equipment = findPermanent(player1, "Mirran Banesplitter");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new MirranBanesplitter());
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {3} moves Mirran Banesplitter to another creature")
    void equipMovesEquipmentToAnotherCreature() {
        Permanent firstCreature = addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent secondCreature = addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new MirranBanesplitter());
        equipment.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int equipmentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(equipment);
        harness.activateAbility(player1, equipmentIndex, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
    }

    @Test
    void flashAllowsCastingDuringOpponentsCombat() {
        Permanent creature = addCreatureReady(player1, new AlabasterHostSanctifier());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.ensurePriority(player1);
        harness.setHand(player1, List.of(new MirranBanesplitter()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Mirran Banesplitter").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    void entersUnattachedWhenOnlyOpponentHasCreatures() {
        harness.addToBattlefield(player2, new AlabasterHostSanctifier());
        harness.setHand(player1, List.of(new MirranBanesplitter()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Mirran Banesplitter").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void remainsUnattachedWhenTargetLeavesBeforeTriggerResolves() {
        Permanent creature = addCreatureReady(player1, new AlabasterHostSanctifier());
        harness.setHand(player1, List.of(new MirranBanesplitter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Mirran Banesplitter").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
