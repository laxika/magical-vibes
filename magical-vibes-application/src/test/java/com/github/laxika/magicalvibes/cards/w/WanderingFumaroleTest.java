package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WanderingFumarole.class)
class WanderingFumaroleTest extends BaseCardTest {

    @Test
    @DisplayName("Wandering Fumarole enters tapped and produces blue or red mana")
    void entersTappedAndAddsChosenMana() {
        harness.setHand(player1, List.of(new WanderingFumarole()));
        harness.playLand(player1, 0);

        Permanent fumarole = findPermanent(player1, "Wandering Fumarole");
        assertThat(fumarole.isTapped()).isTrue();

        fumarole.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Wandering Fumarole becomes a 1/4 blue and red Elemental and can switch its power and toughness")
    void animatesAndSwitchesPowerAndToughness() {
        Permanent fumarole = addReadyFumarole(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, fumarole)).isTrue();
        assertThat(gqs.isLand(gd, fumarole)).isTrue();
        assertThat(gqs.getEffectivePower(gd, fumarole)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fumarole)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, fumarole))
                .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        assertThat(fumarole.getTransientSubtypes()).containsExactly(CardSubtype.ELEMENTAL);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fumarole)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, fumarole)).isEqualTo(1);
    }

    @Test
    @DisplayName("Wandering Fumarole's animation and switch ability end at cleanup")
    void animationAndSwitchEndAtCleanup() {
        Permanent fumarole = addReadyFumarole(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, fumarole)).isFalse();
        assertThat(gqs.isLand(gd, fumarole)).isTrue();
        assertThat(gqs.getEffectivePower(gd, fumarole)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, fumarole)).isEqualTo(0);
        assertThat(fumarole.getTransientSubtypes()).doesNotContain(CardSubtype.ELEMENTAL);
    }

    private Permanent addReadyFumarole(Player player) {
        Permanent permanent = new Permanent(new WanderingFumarole());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
