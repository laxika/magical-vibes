package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JeskaiBannerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the mana ability prompts a choice between blue, red, and white")
    void activatingManaAbilityPromptsColorChoice() {
        addReadyBanner();
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLUE", "RED", "WHITE");
    }

    @Test
    @DisplayName("Choosing a color adds one mana of that color")
    void choosingColorAddsMana() {
        for (String color : List.of("BLUE", "RED", "WHITE")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();
            gd = harness.getGameData();
            addReadyBanner();

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.valueOf(color)))
                    .isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Paying blue, red, and white mana sacrifices Jeskai Banner and draws a card")
    void payingColoredManaSacrificesAndDraws() {
        Permanent banner = addReadyBanner();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(banner);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(banner.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("The draw ability cannot be activated without all three colored mana")
    void cannotActivateDrawAbilityWithoutMana() {
        addReadyBanner();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Jeskai Banner");
    }

    private Permanent addReadyBanner() {
        Permanent banner = new Permanent(new JeskaiBanner());
        banner.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(banner);
        return banner;
    }
}
