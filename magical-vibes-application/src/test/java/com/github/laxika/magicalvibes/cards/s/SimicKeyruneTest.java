package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimicKeyruneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Simic Keyrune adds one green or blue mana")
    void tappingAddsChosenMana() {
        Permanent keyrune = addReadyKeyrune(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(keyrune.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying green and blue mana animates Simic Keyrune")
    void payingGreenAndBlueAnimatesKeyrune() {
        Permanent keyrune = addReadyKeyrune(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, keyrune)).isTrue();
        assertThat(gqs.isArtifact(keyrune)).isTrue();
        assertThat(gqs.getEffectivePower(gd, keyrune)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, keyrune)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, keyrune))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
        assertThat(gqs.hasKeyword(gd, keyrune, Keyword.HEXPROOF)).isTrue();
        assertThat(keyrune.getTransientSubtypes()).contains(CardSubtype.CRAB);
    }

    @Test
    @DisplayName("Simic Keyrune stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent keyrune = addReadyKeyrune(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, keyrune)).isFalse();
        assertThat(gqs.isArtifact(keyrune)).isTrue();
        assertThat(gqs.hasKeyword(gd, keyrune, Keyword.HEXPROOF)).isFalse();
    }

    private Permanent addReadyKeyrune(Player player) {
        Permanent permanent = new Permanent(new SimicKeyrune());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
