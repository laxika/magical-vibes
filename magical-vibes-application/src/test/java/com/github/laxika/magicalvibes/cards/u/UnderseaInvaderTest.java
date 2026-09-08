package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnderseaInvaderTest extends BaseCardTest {

    @Test
    @DisplayName("Undersea Invader enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new UnderseaInvader()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent invader = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(invader.isTapped()).isTrue();
    }
}
