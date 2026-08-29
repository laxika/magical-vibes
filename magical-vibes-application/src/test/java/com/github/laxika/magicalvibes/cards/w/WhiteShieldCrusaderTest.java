package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhiteShieldCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("White Shield Crusader has protection from black")
    void protectionFromBlackPreventsTargeting() {
        Permanent crusader = addReadyCrusader(player2);
        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, crusader.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("White ability grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent crusader = addReadyCrusader(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Two white ability gives +1/+0 until end of turn")
    void boostsPowerUntilEndOfTurn() {
        Permanent crusader = addReadyCrusader(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(2);
    }

    private Permanent addReadyCrusader(Player player) {
        Permanent perm = new Permanent(new WhiteShieldCrusader());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

}
