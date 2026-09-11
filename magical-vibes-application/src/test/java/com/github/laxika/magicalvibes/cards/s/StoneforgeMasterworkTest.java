package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StoneforgeMasterwork.class, GrizzlyBears.class, LlanowarElves.class})
class StoneforgeMasterworkTest extends BaseCardTest {

    @Test
    void boostsForOtherMatchingCreaturesYouControl() {
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent masterwork = addMasterworkReady(player1);
        masterwork.setAttachedTo(equipped.getId());

        assertThat(gqs.getEffectivePower(gd, equipped)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, equipped)).isEqualTo(3);
    }

    @Test
    void boostUpdatesWhenMatchingCreatureIsAdded() {
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent masterwork = addMasterworkReady(player1);
        masterwork.setAttachedTo(equipped.getId());

        assertThat(gqs.getEffectivePower(gd, equipped)).isEqualTo(3);

        addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, equipped)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, equipped)).isEqualTo(4);
    }

    @Test
    void equipAbilityAttachesEquipmentToCreature() {
        Permanent masterwork = addMasterworkReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(masterwork), 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(masterwork.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addMasterworkReady(Player player) {
        Permanent permanent = new Permanent(new StoneforgeMasterwork());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
