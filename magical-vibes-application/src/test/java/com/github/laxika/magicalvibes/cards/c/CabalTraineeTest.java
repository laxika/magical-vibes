package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.n.NantukoMonastery;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CabalTrainee.class, GiantWarthog.class, NantukoMonastery.class})
class CabalTraineeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Cabal Trainee weakens the target creature")
    void sacrificesAndWeakensTargetCreature() {
        harness.addToBattlefield(player1, new CabalTrainee());
        harness.addToBattlefield(player2, new GiantWarthog());
        UUID targetId = harness.getPermanentId(player2, "Giant Warthog");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cabal Trainee");
        harness.assertInGraveyard(player1, "Cabal Trainee");
        Permanent warthog = findPermanent(player2, "Giant Warthog");
        assertThat(warthog.getEffectivePower()).isEqualTo(3);
        assertThat(warthog.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("The power reduction wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new CabalTrainee());
        harness.addToBattlefield(player2, new GiantWarthog());
        UUID targetId = harness.getPermanentId(player2, "Giant Warthog");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent warthog = findPermanent(player2, "Giant Warthog");
        assertThat(warthog.getEffectivePower()).isEqualTo(5);
        assertThat(warthog.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new CabalTrainee());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new NantukoMonastery());
        UUID targetId = land.getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid even when the target leaves before resolution")
    void sacrificeIsPaidWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new CabalTrainee());
        harness.addToBattlefield(player2, new GiantWarthog());
        UUID targetId = harness.getPermanentId(player2, "Giant Warthog");

        harness.activateAbility(player1, 0, null, targetId);
        harness.assertNotOnBattlefield(player1, "Cabal Trainee");
        harness.assertInGraveyard(player1, "Cabal Trainee");

        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(targetId));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Giant Warthog");
    }
}
