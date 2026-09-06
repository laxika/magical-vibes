package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.Roterothopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnabaAncestor.class, AnabaBodyguard.class, Roterothopter.class})
class AnabaAncestorTest extends BaseCardTest {

    @Test
    @DisplayName("Another target Minotaur gets +1/+1 until end of turn")
    void boostsAnotherMinotaur() {
        setupAncestor();
        UUID targetId = harness.getPermanentId(player1, "Anaba Bodyguard");

        harness.activateAbility(player1, 0, null, targetId);
        assertThat(findPermanent(player1, "Anaba Ancestor").isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent minotaur = findPermanent(player1, "Anaba Bodyguard");
        assertThat(minotaur.getPowerModifier()).isEqualTo(1);
        assertThat(minotaur.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can boost a Minotaur an opponent controls")
    void boostsOpponentMinotaur() {
        setupAncestor();
        harness.addToBattlefield(player2, new AnabaBodyguard());
        UUID targetId = harness.getPermanentId(player2, "Anaba Bodyguard");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Anaba Bodyguard").getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        setupAncestor();
        UUID targetId = harness.getPermanentId(player1, "Anaba Bodyguard");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent minotaur = findPermanent(player1, "Anaba Bodyguard");
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
        harness.addToBattlefield(player1, new Roterothopter());
        UUID thopterId = harness.getPermanentId(player1, "Roterothopter");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, thopterId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupAncestor() {
        Permanent ancestor = harness.addToBattlefieldAndReturn(player1, new AnabaAncestor());
        harness.addToBattlefield(player1, new AnabaBodyguard());
        ancestor.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
