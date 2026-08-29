package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VulshokSplitterTest extends BaseCardTest {

    @Test
    @DisplayName("For Mirrodin! creates and attaches a 2/2 Rebel token")
    void forMirrodinCreatesAndAttachesRebel() {
        harness.setHand(player1, List.of(new VulshokSplitter()));
        addManaForVulshokSplitter();

        harness.castArtifact(player1, 0);
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

    private Permanent addSplitterReady(Player player) {
        Permanent permanent = new Permanent(new VulshokSplitter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addManaForVulshokSplitter() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
