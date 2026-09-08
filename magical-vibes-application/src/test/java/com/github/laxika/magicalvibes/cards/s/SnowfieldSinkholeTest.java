package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SnowfieldSinkholeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new SnowfieldSinkhole()));

        harness.playLand(player1, 0);

        Permanent sinkhole = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(sinkhole.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds white or black mana")
    void manaAbilityAddsWhiteOrBlackMana() {
        Permanent sinkhole = addReadySinkhole();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(sinkhole.isTapped()).isTrue();

        sinkhole.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    private Permanent addReadySinkhole() {
        Permanent sinkhole = new Permanent(new SnowfieldSinkhole());
        sinkhole.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sinkhole);
        return sinkhole;
    }
}
