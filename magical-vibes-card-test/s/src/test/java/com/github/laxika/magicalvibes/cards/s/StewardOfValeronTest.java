package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({StewardOfValeron.class})
class StewardOfValeronTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: adds {G}")
    void tapForGreenMana() {
        Permanent steward = addCreatureReady(player1, new StewardOfValeron());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(steward.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new StewardOfValeron());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Summoning sickness prevents tapping for mana")
    void cannotActivateWithSummoningSickness() {
        Permanent steward = harness.addToBattlefieldAndReturn(player1, new StewardOfValeron());
        steward.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(steward.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Vigilance allows attacking and then tapping for mana without leaving combat")
    void canTapForManaWhileAttacking() {
        Permanent steward = addCreatureReady(player1, new StewardOfValeron());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(steward.isTapped()).isFalse();
        assertThat(steward.isAttacking()).isTrue();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(steward.isTapped()).isTrue();
        assertThat(steward.isAttacking()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();

        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Mana is added to the activating controller's pool")
    void addsManaForSecondPlayer() {
        Permanent steward = addCreatureReady(player2, new StewardOfValeron());

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(steward.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
