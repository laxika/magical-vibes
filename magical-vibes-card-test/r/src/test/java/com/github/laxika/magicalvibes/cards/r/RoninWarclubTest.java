package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoninWarclub.class, GnarledMass.class})
class RoninWarclubTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent warclub = harness.addToBattlefieldAndReturn(player1, new RoninWarclub());
        warclub.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Resolving equip attaches Ronin Warclub to target creature")
    void resolvingEquipAttaches() {
        Permanent warclub = harness.addToBattlefieldAndReturn(player1, new RoninWarclub());
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(warclub.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Whenever a creature you control enters, Ronin Warclub attaches to it")
    void attachesToEnteringCreature() {
        Permanent warclub = harness.addToBattlefieldAndReturn(player1, new RoninWarclub());

        harness.setHand(player1, List.of(new GnarledMass()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Gnarled Mass");
        assertThat(warclub.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("A creature an opponent controls entering does not trigger Ronin Warclub")
    void opponentCreatureDoesNotTriggerAttachment() {
        Permanent warclub = harness.addToBattlefieldAndReturn(player1, new RoninWarclub());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GnarledMass()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(warclub.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Entering creature takes the Equipment and its boost from the previous creature")
    void enteringCreatureMovesEquipment() {
        Permanent previous = addCreatureReady(player1, new GnarledMass());
        Permanent warclub = harness.addToBattlefieldAndReturn(player1, new RoninWarclub());
        warclub.setAttachedTo(previous.getId());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GnarledMass());
        assertThat(warclub.getAttachedTo()).isEqualTo(previous.getId());
        harness.passBothPriorities();

        assertThat(warclub.getAttachedTo()).isEqualTo(entering.getId());
        assertThat(gqs.getEffectivePower(gd, previous)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, previous)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(4);
    }

    @Test
    @DisplayName("If the entering creature leaves, the previous attachment is preserved")
    void enteringCreatureLeavesBeforeTriggerResolves() {
        Permanent previous = addCreatureReady(player1, new GnarledMass());
        Permanent warclub = harness.addToBattlefieldAndReturn(player1, new RoninWarclub());
        warclub.setAttachedTo(previous.getId());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GnarledMass());

        gd.playerBattlefields.get(player1.getId()).remove(entering);
        gd.playerGraveyards.get(player1.getId()).add(entering.getCard());
        harness.passBothPriorities();

        assertThat(warclub.getAttachedTo()).isEqualTo(previous.getId());
        assertThat(gqs.getEffectivePower(gd, previous)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, previous)).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple entering creatures attach in trigger resolution order")
    void multipleEnteringCreaturesResolveInStackOrder() {
        Permanent warclub = harness.addToBattlefieldAndReturn(player1, new RoninWarclub());
        Permanent first = harness.enterBattlefieldAndReturn(player1, new GnarledMass());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new GnarledMass());

        harness.passBothPriorities();
        assertThat(warclub.getAttachedTo()).isEqualTo(second.getId());
        harness.passBothPriorities();

        assertThat(warclub.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
    }
}
