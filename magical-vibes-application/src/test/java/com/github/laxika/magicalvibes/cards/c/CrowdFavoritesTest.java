package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
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

@CardUsed({CrowdFavorites.class, GlorySeeker.class, Island.class})
class CrowdFavoritesTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature")
    void tapsTargetCreature() {
        addReadyCrowdFavorites(player1);
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap a creature its controller controls")
    void canTapCreatureItsControllerControls() {
        addReadyCrowdFavorites(player1);
        Permanent target = addCreatureReady(player1, new GlorySeeker());
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

        assertThat(gqs.getEffectivePower(gd, crowdFavorites)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crowdFavorites)).isEqualTo(9);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crowdFavorites)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crowdFavorites)).isEqualTo(4);
    }

    @Test
    @DisplayName("Both abilities can be activated while Crowd Favorites is tapped and summoning sick")
    void abilitiesDoNotRequireTappingCrowdFavorites() {
        Permanent crowdFavorites = harness.addToBattlefieldAndReturn(player1, new CrowdFavorites());
        crowdFavorites.setSummoningSick(true);
        crowdFavorites.tap();
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, crowdFavorites)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crowdFavorites)).isEqualTo(9);
    }

    private Permanent addReadyCrowdFavorites(Player player) {
        return addCreatureReady(player, new CrowdFavorites());
    }
}
