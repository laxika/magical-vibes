package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MegatogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact gives Megatog +3/+3 and trample")
    void sacrificingArtifactBoostsAndGrantsTrample() {
        addReadyMegatog(player1);
        harness.addToBattlefield(player1, new LeoninScimitar());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent megatog = findPermanent(player1, "Megatog");
        assertThat(megatog.getPowerModifier()).isEqualTo(3);
        assertThat(megatog.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, megatog, Keyword.TRAMPLE)).isTrue();
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void boostAndTrampleWearOffAtEndOfTurn() {
        addReadyMegatog(player1);
        harness.addToBattlefield(player1, new LeoninScimitar());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent megatog = findPermanent(player1, "Megatog");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(megatog.getPowerModifier()).isZero();
        assertThat(megatog.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, megatog, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Megatog cannot activate without an artifact to sacrifice")
    void requiresArtifactToSacrifice() {
        addReadyMegatog(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMegatog(Player player) {
        Permanent perm = new Permanent(new Megatog());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

}
