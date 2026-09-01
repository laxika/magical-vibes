package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ImperialMask.class)
class ImperialMaskTest extends BaseCardTest {

    @Test
    @DisplayName("Imperial Mask gives its controller hexproof")
    void givesControllerHexproof() {
        harness.addToBattlefield(player1, new ImperialMask());

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("The team-only copy ability creates no token in a non-team game")
    void doesNotCreateTokenCopyWithoutTeammate() {
        harness.setHand(player1, List.of(new ImperialMask()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Imperial Mask stops giving its controller hexproof when it leaves")
    void losesHexproofWhenRemoved() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new ImperialMask());
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(mask);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
    }
}
