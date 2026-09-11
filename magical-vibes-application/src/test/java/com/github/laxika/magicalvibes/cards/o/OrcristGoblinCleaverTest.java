package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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

    private List<Permanent> treasuresFor(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .toList();
    }
}
