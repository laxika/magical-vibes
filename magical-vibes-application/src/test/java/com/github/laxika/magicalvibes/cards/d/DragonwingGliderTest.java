package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonwingGliderTest extends BaseCardTest {

    @Test
    @DisplayName("For Mirrodin! creates and attaches a 2/2 Rebel token")
    void forMirrodinCreatesAndAttachesRebel() {
        harness.setHand(player1, List.of(new DragonwingGlider()));
        addManaForDragonwingGlider();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rebel = findPermanent(player1, "Rebel");
        Permanent glider = findPermanent(player1, "Dragonwing Glider");

        assertThat(glider.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Equip moves Dragonwing Glider and its bonuses to another creature")
    void equipMovesGliderToAnotherCreature() {
        Permanent glider = addGliderReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();

        addManaForDragonwingGlider();
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(glider.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    private Permanent addGliderReady(Player player) {
        Permanent permanent = new Permanent(new DragonwingGlider());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addManaForDragonwingGlider() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
