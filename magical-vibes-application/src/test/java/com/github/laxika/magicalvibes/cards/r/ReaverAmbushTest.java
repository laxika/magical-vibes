package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReaverAmbushTest extends BaseCardTest {

    private void giveReaverAmbush() {
        harness.setHand(player1, List.of(new ReaverAmbush()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    @Test
    @DisplayName("Exiles a creature with power 3 or less")
    void exilesCreatureWithPowerThreeOrLess() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        giveReaverAmbush();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 3")
    void cannotTargetHighPowerCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        giveReaverAmbush();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeaves() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        giveReaverAmbush();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getId().equals(target.getCard().getId()));
    }
}
