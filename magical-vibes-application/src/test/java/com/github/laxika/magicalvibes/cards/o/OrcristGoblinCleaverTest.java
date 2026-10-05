package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrcristGoblinCleaver.class, GrizzlyBears.class, HillGiant.class, AvianChangeling.class})
class OrcristGoblinCleaverTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and trample")
    void equippedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Combat damage creates one Treasure for each own creature of the chosen type")
    void createsTreasureForEachControlledCreatureOfChosenType() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(treasuresFor(player1)).hasSize(2);
    }

    @Test
    @DisplayName("A Changeling counts as the chosen creature type")
    void changelingCountsAsChosenType() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new AvianChangeling());
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(treasuresFor(player1)).hasSize(1);
    }

    private Permanent addEquipmentReady(Player player) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player, new OrcristGoblinCleaver());
        equipment.setSummoningSick(false);
        return equipment;
    }

    @Test
    void equipAttachesForThreeMana() {
        Permanent equipment = addEquipmentReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void equipmentControllerChoosesTypeAndCreatesTreasures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());
        Permanent equipment = addEquipmentReady(player2);
        equipment.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player2, "GIANT");

        assertThat(treasuresFor(player2)).hasSize(1);
        assertThat(treasuresFor(player1)).isEmpty();
    }

    @Test
    void choosingAnAbsentTypeCreatesNoTreasures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(treasuresFor(player1)).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipmentWithoutAbilitiesDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(attacker.getId());
        equipment.setLosesAllAbilitiesUntilEndOfTurn(true);
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(treasuresFor(player1)).isEmpty();
    }

    @Test
    void countsCreaturesAtResolutionAfterEquipmentLeaves() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(otherBear);
        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(treasuresFor(player1)).hasSize(1);
    }

    private List<Permanent> treasuresFor(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .toList();
    }
}
