package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrowdFavorites.class, GrizzlyBears.class, Island.class})
class CrowdFavoritesTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature")
    void tapsTargetCreature() {
        addReadyCrowdFavorites(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with the tap ability")
    void cannotTargetNoncreaturePermanent() {
        addReadyCrowdFavorites(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The second ability gives Crowd Favorites +0/+5 until end of turn")
    void boostsToughnessUntilEndOfTurn() {
        Permanent crowdFavorites = addReadyCrowdFavorites(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(crowdFavorites.getEffectivePower()).isEqualTo(4);
        assertThat(crowdFavorites.getEffectiveToughness()).isEqualTo(9);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(crowdFavorites.getEffectivePower()).isEqualTo(4);
        assertThat(crowdFavorites.getEffectiveToughness()).isEqualTo(4);
    }

    private Permanent addReadyCrowdFavorites(Player player) {
        Permanent permanent = new Permanent(new CrowdFavorites());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
