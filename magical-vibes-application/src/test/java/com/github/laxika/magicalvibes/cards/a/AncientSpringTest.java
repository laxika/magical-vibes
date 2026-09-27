package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(AncientSpring.class)
class AncientSpringTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new AncientSpring()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one blue mana")
    void tapAddsOneBlueMana() {
        Permanent spring = harness.addToBattlefieldAndReturn(player1, new AncientSpring());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(spring.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ancient Spring");
    }

    @Test
    @DisplayName("Tap and sacrifice adds one white and one black mana and moves the land to the graveyard")
    void sacrificeAddsWhiteAndBlackMana() {
        harness.addToBattlefield(player1, new AncientSpring());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Ancient Spring");
        harness.assertInGraveyard(player1, "Ancient Spring");
    }

    @Test
    @DisplayName("Cannot activate the sacrifice ability after the land has been tapped")
    void cannotActivateSacrificeAbilityAfterTapping() {
        Permanent spring = harness.addToBattlefieldAndReturn(player1, new AncientSpring());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(spring.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertOnBattlefield(player1, "Ancient Spring");
    }
}
