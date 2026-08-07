package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScablandTest extends BaseCardTest {

    @Test
    @DisplayName("Scabland enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new Scabland()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findPermanent(player1, "Scabland").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for colorless mana adds {C} and deals no damage")
    void tapForColorlessMana() {
        Permanent land = addScablandReady(player1);
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The colored ability prompts a choice between red and white")
    void coloredAbilityPromptsColorChoice() {
        addScablandReady(player1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("RED", "WHITE");
    }

    @Test
    @DisplayName("Choosing a color adds that mana and deals 1 damage to the controller")
    void choosingColorAddsManaAndDealsDamage() {
        for (String color : new String[]{"RED", "WHITE"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent land = addScablandReady(player1);
            GameData gd = harness.getGameData();
            int lifeBefore = gd.playerLifeTotals.get(player1.getId());

            harness.activateAbility(player1, 0, 1, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.valueOf(color))).isEqualTo(1);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
            assertThat(land.isTapped()).isTrue();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Cannot activate a second mana ability while tapped")
    void cannotActivateWhileTapped() {
        addScablandReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addScablandReady(Player player) {
        Permanent perm = new Permanent(new Scabland());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
