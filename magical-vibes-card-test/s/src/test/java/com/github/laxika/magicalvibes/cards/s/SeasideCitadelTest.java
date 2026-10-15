package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SeasideCitadel.class})
class SeasideCitadelTest extends BaseCardTest {

    @Test
    @DisplayName("Seaside Citadel enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new SeasideCitadel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent citadel = findPermanent(player1, "Seaside Citadel");
        assertThat(citadel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability prompts a choice between green, white, and blue")
    void activatingPromptsColorChoice() {
        addCitadelReady(player1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "WHITE", "BLUE");
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color and taps the land")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"GREEN", "WHITE", "BLUE"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent citadel = addCitadelReady(player1);
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(citadel.isTapped()).isTrue();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Seaside Citadel enters tapped even when it is not played from hand")
    void entersTappedWithoutBeingPlayed() {
        Permanent citadel = harness.enterBattlefieldAndReturn(player1, new SeasideCitadel());

        assertThat(citadel.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Seaside Citadel cannot activate its mana ability")
    void cannotActivateWhileTapped() {
        harness.enterBattlefieldAndReturn(player1, new SeasideCitadel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("After untapping, Seaside Citadel produces exactly one chosen mana and cannot tap again")
    void producesOnlyChosenManaAfterUntapping() {
        Permanent citadel = harness.enterBattlefieldAndReturn(player1, new SeasideCitadel());
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color))
                    .isEqualTo(color == ManaColor.WHITE ? 1 : 0);
        }
        assertThat(citadel.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCitadelReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SeasideCitadel());
        perm.setSummoningSick(false);
        return perm;
    }
}
