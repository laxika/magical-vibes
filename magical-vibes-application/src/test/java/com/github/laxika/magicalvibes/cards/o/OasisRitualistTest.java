package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OasisRitualistTest extends BaseCardTest {

    @Test
    @DisplayName("First ability taps and adds one mana of the chosen color without exerting")
    void firstAbilityAddsOneManaWithoutExert() {
        harness.addToBattlefield(player1, new OasisRitualist());
        Permanent ritualist = gd.playerBattlefields.get(player1.getId()).getFirst();
        ritualist.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(ritualist.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(ritualist.getSkipUntapCount()).isEqualTo(0);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Second ability adds two mana of the chosen color and exerts")
    void secondAbilityAddsTwoManaAndExerts() {
        harness.addToBattlefield(player1, new OasisRitualist());
        Permanent ritualist = gd.playerBattlefields.get(player1.getId()).getFirst();
        ritualist.setSummoningSick(false);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(ritualist.isTapped()).isTrue();
        assertThat(ritualist.getSkipUntapCount()).isGreaterThan(0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new OasisRitualist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }
}
