package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlossomingSands.class})
class BlossomingSandsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BlossomingSands()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Mana ability prompts for green or white")
    void manaAbilityPromptsForGreenOrWhite() {
        addReadySands(player1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "WHITE");
    }

    @Test
    @DisplayName("Choosing green adds one green mana")
    void choosingGreenAddsMana() {
        Permanent sands = addReadySands(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(sands.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing white adds one white mana")
    void choosingWhiteAddsMana() {
        Permanent sands = addReadySands(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(sands.isTapped()).isTrue();
    }

    private Permanent addReadySands(Player player) {
        return addCreatureReady(player, new BlossomingSands());
    }

    @Test
    @DisplayName("Entering without being played gains life only for its controller")
    void enteringWithoutBeingPlayedGainsLifeForController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent sands = harness.enterBattlefieldAndReturn(player2, new BlossomingSands());

        assertThat(sands.isTapped()).isTrue();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Life gain trigger resolves after the land leaves the battlefield")
    void lifeGainResolvesAfterLandLeaves() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BlossomingSands()));
        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        Permanent sands = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(sands.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An untapped land can produce mana the turn it enters")
    void manaAbilityDoesNotRequireClearingSummoningSickness() {
        Permanent sands = harness.addToBattlefieldAndReturn(player1, new BlossomingSands());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(sands.isTapped()).isTrue();
    }
}
