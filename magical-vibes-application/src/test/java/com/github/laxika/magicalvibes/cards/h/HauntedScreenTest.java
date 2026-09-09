package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HauntedScreen.class})
class HauntedScreenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds white or black mana")
    void tapsForWhiteOrBlackMana() {
        harness.addToBattlefield(player1, new HauntedScreen());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping and paying 1 life adds green, blue, or red mana")
    void tapsForManaAfterPayingLife() {
        harness.addToBattlefield(player1, new HauntedScreen());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The seven-mana ability permanently animates the screen with seven counters")
    void animatesWithSevenCounters() {
        Permanent screen = harness.addToBattlefieldAndReturn(player1, new HauntedScreen());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(screen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(gqs.isArtifact(screen)).isTrue();
        assertThat(gqs.isCreature(gd, screen)).isTrue();
        assertThat(screen.getGrantedSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(gqs.getEffectivePower(gd, screen)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, screen)).isEqualTo(7);
    }

    @Test
    @DisplayName("The seven-mana ability can only be activated once")
    void animatingCanOnlyBeActivatedOnce() {
        harness.addToBattlefield(player1, new HauntedScreen());
        harness.addMana(player1, ManaColor.COLORLESS, 14);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }
}
