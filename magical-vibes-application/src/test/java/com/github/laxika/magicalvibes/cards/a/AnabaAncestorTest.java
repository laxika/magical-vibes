package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HurloonMinotaur;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnabaAncestorTest extends BaseCardTest {

    @Test
    @DisplayName("Another target Minotaur gets +1/+1 until end of turn")
    void boostsAnotherMinotaur() {
        setupAncestor();
        UUID targetId = harness.getPermanentId(player1, "Hurloon Minotaur");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent minotaur = findPermanent(player1, "Hurloon Minotaur");
        assertThat(minotaur.getPowerModifier()).isEqualTo(1);
        assertThat(minotaur.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can boost a Minotaur an opponent controls")
    void boostsOpponentMinotaur() {
        setupAncestor();
        harness.addToBattlefield(player2, new HurloonMinotaur());
        UUID targetId = harness.getPermanentId(player2, "Hurloon Minotaur");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Hurloon Minotaur").getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        setupAncestor();
        UUID targetId = harness.getPermanentId(player1, "Hurloon Minotaur");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent minotaur = findPermanent(player1, "Hurloon Minotaur");
        assertThat(minotaur.getPowerModifier()).isEqualTo(0);
        assertThat(minotaur.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        setupAncestor();
        UUID selfId = harness.getPermanentId(player1, "Anaba Ancestor");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, selfId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Minotaur creature")
    void cannotTargetNonMinotaur() {
        setupAncestor();
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupAncestor() {
        harness.addToBattlefield(player1, new AnabaAncestor());
        harness.addToBattlefield(player1, new HurloonMinotaur());
        findPermanent(player1, "Anaba Ancestor").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
