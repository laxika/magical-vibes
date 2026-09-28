package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PowerArmor.class, RagingKavu.class, Forest.class, Island.class, Mountain.class})
class PowerArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the target by one for each distinct basic land type controlled")
    void boostsTargetByDomain() {
        setupBattlefield();

        UUID targetId = findPermanent(player1, "Raging Kavu").getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Power Armor").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        Permanent kavu = findPermanent(player1, "Raging Kavu");
        assertThat(kavu.getPowerModifier()).isEqualTo(3);
        assertThat(kavu.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target an opponent's creature using the controller's domain count")
    void boostsOpponentCreatureUsingControllerDomain() {
        setupBattlefield();
        Permanent opponentKavu = harness.addToBattlefieldAndReturn(player2, new RagingKavu());

        harness.activateAbility(player1, 0, null, opponentKavu.getId());
        harness.passBothPriorities();

        assertThat(opponentKavu.getPowerModifier()).isEqualTo(3);
        assertThat(opponentKavu.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        setupBattlefield();

        UUID targetId = findPermanent(player1, "Raging Kavu").getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Raging Kavu");
        assertThat(kavu.getPowerModifier()).isEqualTo(0);
        assertThat(kavu.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        setupBattlefield();

        UUID forestId = findPermanent(player1, "Forest").getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupBattlefield() {
        harness.addToBattlefield(player1, new PowerArmor());
        harness.addToBattlefield(player1, new RagingKavu());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
