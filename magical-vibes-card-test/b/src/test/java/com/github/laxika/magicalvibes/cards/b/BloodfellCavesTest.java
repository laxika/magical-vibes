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

@CardUsed({BloodfellCaves.class})
class BloodfellCavesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BloodfellCaves()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Mana ability prompts for black or red")
    void manaAbilityPromptsForBlackOrRed() {
        addReadyCaves(player1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLACK", "RED");
    }

    @Test
    @DisplayName("Choosing black adds one black mana")
    void choosingBlackAddsMana() {
        Permanent caves = addReadyCaves(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(caves.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing red adds one red mana")
    void choosingRedAddsMana() {
        Permanent caves = addReadyCaves(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(caves.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Life gain waits for resolution and survives the land leaving")
    void lifeGainResolvesAfterLandLeavesBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodfellCaves()));

        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        Permanent caves = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(caves.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being played still enters tapped and gains life")
    void enteringWithoutBeingPlayedTriggersLifeGain() {
        harness.setLife(player2, 20);
        Permanent caves = harness.enterBattlefieldAndReturn(player2, new BloodfellCaves());

        assertThat(caves.isTapped()).isTrue();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 21);
    }

    private Permanent addReadyCaves(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BloodfellCaves());
        perm.setSummoningSick(false);
        return perm;
    }
}
