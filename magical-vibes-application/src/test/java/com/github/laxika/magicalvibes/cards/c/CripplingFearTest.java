package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
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

class CripplingFearTest extends BaseCardTest {

    @Test
    @DisplayName("Weakens creatures that are not of the chosen type on every battlefield")
    void weakensCreaturesNotOfChosenType() {
        Permanent ownBear = addReadyCreature(player1, new GrizzlyBears());
        Permanent opposingBear = addReadyCreature(player2, new GrizzlyBears());
        Permanent ownAvatar = addReadyCreature(player1, new AvatarOfMight());
        Permanent opposingAvatar = addReadyCreature(player2, new AvatarOfMight());

        castCripplingFear(player1);
        harness.handleListChoice(player1, "BEAR");

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opposingAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposingAvatar)).isEqualTo(5);
    }

    @Test
    @DisplayName("A Changeling counts as the chosen type")
    void changelingCountsAsChosenType() {
        Permanent changeling = addReadyCreature(player2, new AvianChangeling());
        Permanent bear = addReadyCreature(player2, new GrizzlyBears());

        castCripplingFear(player1);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(changeling).doesNotContain(bear);
    }

    @Test
    @DisplayName("The -3/-3 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent avatar = addReadyCreature(player2, new AvatarOfMight());

        castCripplingFear(player1);
        harness.handleListChoice(player1, "BEAR");

        assertThat(avatar.getPowerModifier()).isEqualTo(-3);
        assertThat(avatar.getToughnessModifier()).isEqualTo(-3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(avatar.getPowerModifier()).isEqualTo(0);
        assertThat(avatar.getToughnessModifier()).isEqualTo(0);
    }

    private void castCripplingFear(Player player) {
        harness.setHand(player, List.of(new CripplingFear()));
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castSorcery(player, 0, 0);
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
