package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraypeltRefuge.class})
class GraypeltRefugeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GraypeltRefuge()));

        harness.playLand(player1, 0);

        Permanent refuge = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(refuge.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Mana ability adds green mana when green is chosen")
    void manaAbilityAddsGreenMana() {
        Permanent refuge = addReadyRefuge();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(refuge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds white mana when white is chosen")
    void manaAbilityAddsWhiteMana() {
        Permanent refuge = addReadyRefuge();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(refuge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Life gain waits for the enter trigger to resolve")
    void lifeGainUsesTheStack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GraypeltRefuge()));

        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second player gains life when they play the refuge")
    void secondPlayerGainsLife() {
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new GraypeltRefuge()));

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped refuge cannot activate its mana ability")
    void tappedRefugeCannotProduceMana() {
        Permanent refuge = addReadyRefuge();
        refuge.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("A newly controlled noncreature refuge can produce mana immediately when untapped")
    void noncreatureLandIgnoresSummoningSickness() {
        Permanent refuge = harness.addToBattlefieldAndReturn(player1, new GraypeltRefuge());
        refuge.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(refuge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyRefuge() {
        Permanent refuge = harness.addToBattlefieldAndReturn(player1, new GraypeltRefuge());
        refuge.setSummoningSick(false);
        return refuge;
    }
}
