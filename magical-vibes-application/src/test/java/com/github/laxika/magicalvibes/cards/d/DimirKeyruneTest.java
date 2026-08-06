package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DimirKeyruneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Dimir Keyrune adds one blue or black mana")
    void tappingAddsChosenMana() {
        Permanent keyrune = addReadyKeyrune(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(keyrune.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying blue and black animates Dimir Keyrune and makes it unblockable")
    void payingAnimatesAndMakesUnblockable() {
        Permanent keyrune = addReadyKeyrune(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, keyrune)).isTrue();
        assertThat(gqs.isArtifact(keyrune)).isTrue();
        assertThat(gqs.getEffectivePower(gd, keyrune)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, keyrune)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, keyrune))
                .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
        assertThat(keyrune.getTransientSubtypes()).contains(CardSubtype.HORROR);
        assertThat(keyrune.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Animation and unblockability wear off at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent keyrune = addReadyKeyrune(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, keyrune)).isFalse();
        assertThat(gqs.isArtifact(keyrune)).isTrue();
        assertThat(keyrune.isCantBeBlocked()).isFalse();
    }

    private Permanent addReadyKeyrune(Player player) {
        Permanent permanent = new Permanent(new DimirKeyrune());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
