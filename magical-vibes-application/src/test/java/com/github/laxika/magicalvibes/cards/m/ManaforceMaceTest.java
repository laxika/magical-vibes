package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManaforceMaceTest extends BaseCardTest {

    // ===== Domain boost =====

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each basic land type among controlled lands")
    void boostsPerBasicLandType() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent mace = new Permanent(new ManaforceMace());
        mace.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(mace);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());

        // 2/2 base + 3 distinct basic land types * +1/+1 = 5/5
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Duplicate basic land types are counted once")
    void duplicateBasicTypesCountOnce() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent mace = new Permanent(new ManaforceMace());
        mace.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(mace);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        // Three Forests = one distinct type = +1/+1 -> 3/3
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("No basic land types means no boost")
    void noBasicLandTypesNoBoost() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent mace = new Permanent(new ManaforceMace());
        mace.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(mace);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Domain counts equipment controller's basic land types, not the opponent's")
    void doesNotCountOpponentLands() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent mace = new Permanent(new ManaforceMace());
        mace.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(mace);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Plains());

        // Only player1's single Forest type counts -> 3/3
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    // ===== Equip {3} =====

    @Test
    @DisplayName("Resolving equip ability attaches the mace and grants the domain boost")
    void equipAttachesAndBoosts() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        Permanent creature = addReadyCreature(player1);
        Permanent mace = addMaceReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int maceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mace);
        harness.activateAbility(player1, maceIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mace.getAttachedTo()).isEqualTo(creature.getId());
        // 2/2 base + 2 distinct basic land types = 4/4
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipped creature loses the boost when the mace leaves the battlefield")
    void creatureLosesBoostWhenMaceRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent mace = new Permanent(new ManaforceMace());
        mace.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(mace);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(mace);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent addMaceReady(Player player) {
        Permanent perm = new Permanent(new ManaforceMace());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
