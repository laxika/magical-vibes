package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.n.NantukoMonastery;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SylvanSafekeeper.class, NantukoMonastery.class, GiantWarthog.class})
class SylvanSafekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land grants shroud to a creature you control")
    void sacrificeLandGrantsShroudToControlledCreature() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new NantukoMonastery());
        Permanent warthog = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());

        harness.activateAbility(player1, 0, null, warthog.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nantuko Monastery");
        assertThat(warthog.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("The granted shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new NantukoMonastery());
        Permanent warthog = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());

        harness.activateAbility(player1, 0, null, warthog.getId());
        harness.passBothPriorities();
        assertThat(warthog.hasKeyword(Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warthog.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature cannot be targeted")
    void onlyControlledCreatureCanBeTargeted() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new NantukoMonastery());
        Permanent enemyWarthog = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enemyWarthog.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("A land you control cannot be targeted")
    void onlyCreaturesCanBeTargeted() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NantukoMonastery());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        harness.assertOnBattlefield(player1, "Nantuko Monastery");
    }

    @Test
    @DisplayName("Granted shroud prevents later abilities from targeting the creature")
    void grantedShroudPreventsLaterTargeting() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new NantukoMonastery());
        Permanent warthog = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());

        harness.activateAbility(player1, 0, null, warthog.getId());
        harness.passBothPriorities();

        assertThat(warthog.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, warthog.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot be activated without a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        Permanent warthog = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, warthog.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
