package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BenalishHeraldsTest extends BaseCardTest {

    @Test
    @DisplayName("Pays {3}{U} and taps to draw a card")
    void paysManaAndTapsToDrawACard() {
        Permanent heralds = addReadyHeralds();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);

        assertThat(heralds.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private Permanent addReadyHeralds() {
        harness.addToBattlefield(player1, new BenalishHeralds());
        Permanent heralds = gd.playerBattlefields.get(player1.getId()).getFirst();
        heralds.setSummoningSick(false);
        return heralds;
    }
}
