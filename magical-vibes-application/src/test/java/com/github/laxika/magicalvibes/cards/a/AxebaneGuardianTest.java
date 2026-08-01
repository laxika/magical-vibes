package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfGranite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AxebaneGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Alone it adds one mana of a chosen color (it counts itself)")
    void aloneAddsOneMana() {
        harness.addToBattlefield(player1, new AxebaneGuardian());
        Permanent guardian = gd.playerBattlefields.get(player1.getId()).getFirst();
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
        harness.addToBattlefield(player1, new AxebaneGuardian());
        harness.addToBattlefield(player1, new WallOfGranite());
        harness.addToBattlefield(player1, new WallOfGranite());
        Permanent guardian = gd.playerBattlefields.get(player1.getId()).getFirst();
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
        harness.addToBattlefield(player1, new AxebaneGuardian());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new WallOfGranite());
        Permanent guardian = gd.playerBattlefields.get(player1.getId()).getFirst();
        guardian.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
