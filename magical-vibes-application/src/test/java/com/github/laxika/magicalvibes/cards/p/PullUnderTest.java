package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PullUnderTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -5/-5 and dies when toughness drops to 0 or less")
    void killsSmallCreature() {
        Permanent target = addCreature(player2);
        castPullUnder(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("A surviving creature's -5/-5 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addBigCreature(player2);
        castPullUnder(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
    }

    @Test
    @DisplayName("Can target a creature you control")
    void canTargetOwnCreature() {
        Permanent own = addCreature(player1);
        castPullUnder(own);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(own);
    }

    private void castPullUnder(Permanent target) {
        harness.setHand(player1, List.of(new PullUnder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player) {
        return addPermanent(player, new Permanent(new GrizzlyBears()));
    }

    private Permanent addBigCreature(Player player) {
        return addPermanent(player, new Permanent(new AvatarOfMight()));
    }

    private Permanent addPermanent(Player player, Permanent permanent) {
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
