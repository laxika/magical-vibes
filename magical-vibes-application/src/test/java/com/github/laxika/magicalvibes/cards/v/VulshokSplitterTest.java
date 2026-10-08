package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VulshokSplitter.class, GrizzlyBears.class})
class VulshokSplitterTest extends BaseCardTest {

    @Test
    @DisplayName("For Mirrodin! creates and attaches a 2/2 Rebel token")
    void forMirrodinCreatesAndAttachesRebel() {
        harness.castFromHand(player1, new VulshokSplitter(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rebel = findPermanent(player1, "Rebel");
        Permanent splitter = findPermanent(player1, "Vulshok Splitter");

        assertThat(splitter.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsPlusTwoPower() {
        Permanent splitter = addSplitterReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        splitter.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip moves Vulshok Splitter and its bonus to another creature")
    void equipMovesSplitterToAnotherCreature() {
        Permanent splitter = addSplitterReady(player1);
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());
        splitter.setAttachedTo(creature1.getId());
        harness.forceActivePlayer(player1);

        addManaForVulshokSplitter();
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(splitter.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(4);
    }

    @Test
    @DisplayName("For Mirrodin! still creates a Rebel after the Equipment leaves")
    void createsRebelAfterEquipmentLeaves() {
        harness.castFromHand(player1, new VulshokSplitter(), "{3}{R}");
        harness.passBothPriorities();

        Permanent splitter = findPermanent(player1, "Vulshok Splitter");
        assertThat(countPermanents(player1, "Rebel")).isZero();
        gd.playerBattlefields.get(player1.getId()).remove(splitter);
        gd.playerGraveyards.get(player1.getId()).add(splitter.getCard());
        harness.passBothPriorities();

        Permanent rebel = findPermanent(player1, "Rebel");
        assertThat(countPermanents(player1, "Rebel")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Moving the Equipment away leaves the Rebel alive without the bonus")
    void rebelSurvivesMovingEquipment() {
        harness.castFromHand(player1, new VulshokSplitter(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent splitter = findPermanent(player1, "Vulshok Splitter");
        Permanent rebel = findPermanent(player1, "Rebel");
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(splitter.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(findPermanent(player1, "Rebel")).isSameAs(rebel);
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentsCreature() {
        Permanent splitter = addSplitterReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addManaForVulshokSplitter();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(splitter.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRejectsCombatTiming() {
        Permanent splitter = addSplitterReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        addManaForVulshokSplitter();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(splitter.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip requires red mana in addition to its generic cost")
    void equipRequiresRedMana() {
        Permanent splitter = addSplitterReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(splitter.getAttachedTo()).isNull();
    }

    private Permanent addSplitterReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new VulshokSplitter());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addManaForVulshokSplitter() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
