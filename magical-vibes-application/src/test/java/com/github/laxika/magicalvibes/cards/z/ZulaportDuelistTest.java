package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZulaportDuelist.class, Forest.class, GrizzlyBears.class})
class ZulaportDuelistTest extends BaseCardTest {

    @Test
    void debuffsTargetAndMillsItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ZulaportDuelist()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void mayChooseNoTarget() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ZulaportDuelist()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ZulaportDuelist()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
