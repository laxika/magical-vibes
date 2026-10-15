package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FleshToDust;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({AvariceAmulet.class, RuneclawBear.class, FleshToDust.class})
class AvariceAmuletTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0 and has vigilance")
    void equippedCreatureGetsBoostAndVigilance() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent amulet = addCreatureReady(player1, new AvariceAmulet());
        amulet.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creature gets no boost or vigilance")
    void unequippedCreatureUnaffected() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player1, new AvariceAmulet());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Equipped creature's controller draws a card at the beginning of their upkeep")
    void equippedCreatureDrawsOnUpkeep() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent amulet = addCreatureReady(player1, new AvariceAmulet());
        amulet.setAttachedTo(creature.getId());

        harness.setLibrary(player1, List.of(new RuneclawBear()));
        int before = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
    }

    @Test
    @DisplayName("No upkeep draw while the Equipment is unattached")
    void noDrawWhenUnattached() {
        addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player1, new AvariceAmulet());

        harness.setLibrary(player1, List.of(new RuneclawBear()));
        int before = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before);
    }

    @Test
    @DisplayName("Whenever equipped creature dies, target opponent gains control of the Equipment")
    void deathTriggerHandsEquipmentToOpponent() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent amulet = addCreatureReady(player1, new AvariceAmulet());
        amulet.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FleshToDust()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities(); // Flesh to Dust resolves, creature dies, death trigger queued

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve the control-change trigger

        harness.assertOnBattlefield(player2, "Avarice Amulet");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(amulet);
    }

    @Test
    @DisplayName("Amulet stays put when a creature it is not attached to dies")
    void noTriggerForUnequippedCreatureDeath() {
        Permanent equipped = addCreatureReady(player1, new RuneclawBear());
        Permanent other = addCreatureReady(player1, new RuneclawBear());
        Permanent amulet = addCreatureReady(player1, new AvariceAmulet());
        amulet.setAttachedTo(equipped.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FleshToDust()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castInstant(player2, 0, other.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avarice Amulet");
        assertThat(amulet.getAttachedTo()).isEqualTo(equipped.getId());
    }

    @Test
    @DisplayName("Equip {2} attaches the Amulet to a creature you control")
    void equipAttachesToCreature() {
        Permanent amulet = addCreatureReady(player1, new AvariceAmulet());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(amulet.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The death trigger cannot target the Equipment's controller")
    void deathTriggerRejectsItsController() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent amulet = addCreatureReady(player1, new AvariceAmulet());
        amulet.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new FleshToDust()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Avarice Amulet");
    }

    @Test
    @DisplayName("The creature's controller draws even when the opponent controls the Equipment")
    void creatureControllerDrawsWithOpponentsEquipment() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent amulet = addCreatureReady(player2, new AvariceAmulet());
        amulet.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new RuneclawBear()));
        int before = gd.playerHands.get(player1.getId()).size();
        int opponentBefore = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentBefore);
    }

    @Test
    @DisplayName("The Equipment's controller does not draw on their upkeep for an opponent's creature")
    void equipmentControllerDoesNotDrawForOpponentsCreature() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent amulet = addCreatureReady(player2, new AvariceAmulet());
        amulet.setAttachedTo(creature.getId());
        harness.setLibrary(player2, List.of(new RuneclawBear()));
        int before = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(before);
    }

    @Test
    @DisplayName("An upkeep draw already on the stack resolves after the Equipment is detached")
    void upkeepDrawSurvivesDetachment() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent amulet = addCreatureReady(player1, new AvariceAmulet());
        amulet.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new RuneclawBear()));
        int before = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        amulet.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
    }

    @Test
    @DisplayName("The Equipment's controller chooses the opponent when an opponent's equipped creature dies")
    void equipmentControllerControlsDeathTrigger() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        Permanent amulet = addCreatureReady(player1, new AvariceAmulet());
        amulet.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new FleshToDust()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Avarice Amulet");
        assertThat(amulet.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Casting the Equipment does not require a target")
    void castsWithoutTarget() {
        harness.setHand(player1, List.of(new AvariceAmulet()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avarice Amulet");
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentsCreature() {
        Permanent amulet = addCreatureReady(player1, new AvariceAmulet());
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(amulet.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated during upkeep")
    void equipRequiresSorceryTiming() {
        Permanent amulet = addCreatureReady(player1, new AvariceAmulet());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(amulet.getAttachedTo()).isNull();
    }
}
