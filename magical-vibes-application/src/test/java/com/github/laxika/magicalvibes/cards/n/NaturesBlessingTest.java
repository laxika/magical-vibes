package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NaturesBlessingTest extends BaseCardTest {

    private static final String COUNTER_MODE = "Put a +1/+1 counter on it";
    private static final String TRAMPLE_MODE = "It gains trample";
    private static final String BANDING_MODE = "It gains banding";

    private Permanent setUpBlessing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new NaturesBlessing());
        return harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
    }

    private void activate(Player player, Permanent target) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.setHand(player, List.of(new GrizzlyBears()));

        harness.activateAbility(player, 0, null, target.getId());
        harness.handleCardChosen(player, 0); // pay the discard cost
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on the target creature")
    void counterMode() {
        Permanent bears = setUpBlessing();
        int power = gqs.getEffectivePower(gd, bears);
        int toughness = gqs.getEffectiveToughness(gd, bears);

        activate(player1, bears);
        harness.handleListChoice(player1, COUNTER_MODE);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(toughness + 1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Trample mode grants trample and it lasts past end of turn")
    void trampleModeLastsIndefinitely() {
        Permanent bears = setUpBlessing();

        activate(player1, bears);
        harness.handleListChoice(player1, TRAMPLE_MODE);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Banding mode grants banding, not the other modes' keywords")
    void bandingMode() {
        Permanent bears = setUpBlessing();

        activate(player1, bears);
        harness.handleListChoice(player1, BANDING_MODE);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.BANDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The ability can only target a creature")
    void cannotTargetNoncreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent blessing = harness.addToBattlefieldAndReturn(player1, new NaturesBlessing());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blessing.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
