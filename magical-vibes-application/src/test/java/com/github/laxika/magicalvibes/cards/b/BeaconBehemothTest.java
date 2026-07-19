package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeaconBehemothTest extends BaseCardTest {

    // ===== Grant vigilance to a power 5+ creature =====

    @Test
    @DisplayName("Activating grants vigilance to a target creature with power 5 or greater")
    void grantsVigilanceToBigCreature() {
        addReady(player1, new BeaconBehemoth());
        Permanent avatar = addReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, avatar, Keyword.VIGILANCE)).isTrue();
    }

    // ===== Wears off =====

    @Test
    @DisplayName("Vigilance wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addReady(player1, new BeaconBehemoth());
        Permanent avatar = addReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, avatar, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Illegal target =====

    @Test
    @DisplayName("Cannot target a creature with power less than 5")
    void cannotTargetSmallCreature() {
        addReady(player1, new BeaconBehemoth());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 5 or greater");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
