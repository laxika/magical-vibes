package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SwiftwaterCliffs.class})
class SwiftwaterCliffsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SwiftwaterCliffs()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Mana ability prompts for blue or red")
    void manaAbilityPromptsForBlueOrRed() {
        addReadyCliffs(player1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLUE", "RED");
    }

    @Test
    @DisplayName("Choosing blue adds one blue mana")
    void choosingBlueAddsMana() {
        Permanent cliffs = addReadyCliffs(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(cliffs.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing red adds one red mana")
    void choosingRedAddsMana() {
        Permanent cliffs = addReadyCliffs(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(cliffs.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Life gain waits for the entry trigger to resolve")
    void lifeGainUsesTheStack() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SwiftwaterCliffs()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entry trigger still gains life after the land leaves")
    void entryTriggerSurvivesSourceLeaving() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SwiftwaterCliffs()));
        harness.playLand(player1, 0);
        Permanent cliffs = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(cliffs);
        harness.setGraveyard(player1, List.of(cliffs.getCard()));

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being played gains life for its controller")
    void enteringUnderOtherPlayersControlGainsLifeForThatPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        Permanent cliffs = harness.enterBattlefieldAndReturn(player2, new SwiftwaterCliffs());

        assertThat(cliffs.isTapped()).isTrue();
        harness.assertLife(player2, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 11);
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addReadyCliffs(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SwiftwaterCliffs());
        perm.setSummoningSick(false);
        return perm;
    }
}
