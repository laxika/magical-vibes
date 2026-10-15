package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcaneSanctum.class})
class ArcaneSanctumTest extends BaseCardTest {

    @Test
    @DisplayName("Arcane Sanctum enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new ArcaneSanctum()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent sanctum = findPermanent(player1, "Arcane Sanctum");
        assertThat(sanctum.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability prompts a choice between white, blue, and black")
    void activatingPromptsColorChoice() {
        addSanctumReady(player1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK");
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color and taps the land")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"WHITE", "BLUE", "BLACK"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent sanctum = addSanctumReady(player1);
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(sanctum.isTapped()).isTrue();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    private Permanent addSanctumReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ArcaneSanctum());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("A tapped Arcane Sanctum cannot produce mana")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new ArcaneSanctum()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }

    @Test
    @DisplayName("Arcane Sanctum can produce mana after untapping")
    void producesManaAfterUntapping() {
        harness.setHand(player1, List.of(new ArcaneSanctum()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Arcane Sanctum").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
