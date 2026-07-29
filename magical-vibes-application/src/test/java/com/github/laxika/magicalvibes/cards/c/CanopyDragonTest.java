package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CanopyDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Canopy Dragon has trample and no flying by default")
    void tramplerByDefault() {
        Permanent dragon = addReadyDragon(player1);

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("{1}{G}: Canopy Dragon gains flying and loses trample")
    void activationSwapsTrampleForFlying() {
        Permanent dragon = addReadyDragon(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The swap wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent dragon = addReadyDragon(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addReadyDragon(Player player) {
        Permanent perm = new Permanent(new CanopyDragon());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
