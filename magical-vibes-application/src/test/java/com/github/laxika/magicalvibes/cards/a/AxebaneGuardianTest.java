package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GatecreeperVine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AxebaneGuardian.class, GatecreeperVine.class, AxebaneStag.class})
class AxebaneGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Alone it adds one mana of a chosen color (it counts itself)")
    void aloneAddsOneMana() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new AxebaneGuardian());
        guardian.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(guardian.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each mana's color is chosen separately, one per defender")
    void manaCanBeAnyCombinationOfColors() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new AxebaneGuardian());
        harness.addToBattlefield(player1, new GatecreeperVine());
        harness.addToBattlefield(player1, new GatecreeperVine());
        guardian.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Creatures without defender, and opponents' defenders, are not counted")
    void ignoresNonDefendersAndOpponentDefenders() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new AxebaneGuardian());
        harness.addToBattlefield(player1, new AxebaneStag());
        harness.addToBattlefield(player2, new GatecreeperVine());
        guardian.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the tap ability")
    void summoningSicknessPreventsActivation() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new AxebaneGuardian());
        guardian.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(guardian.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Mana resolves without using the stack and a tapped Guardian cannot activate again")
    void resolvesImmediatelyAndRequiresUntappedSource() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new AxebaneGuardian());
        guardian.setSummoningSick(false);
        Permanent vine = harness.addToBattlefieldAndReturn(player1, new GatecreeperVine());
        vine.setTapped(true);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
