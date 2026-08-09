package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoxDiamondTest extends BaseCardTest {

    @Test
    @DisplayName("Entering may discard a land and put Mox Diamond onto the battlefield")
    void entersByDiscardingLand() {
        harness.setHand(player1, List.of(new MoxDiamond(), new Forest(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mox Diamond");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the land discard puts Mox Diamond into its owner's graveyard")
    void decliningLandDiscardSendsItToGraveyard() {
        harness.setHand(player1, List.of(new MoxDiamond(), new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Mox Diamond");
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Mox Diamond");
    }

    @Test
    @DisplayName("Without a land in hand Mox Diamond goes straight to the graveyard")
    void noLandSendsItToGraveyardWithoutPrompt() {
        harness.setHand(player1, List.of(new MoxDiamond(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Mox Diamond");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tapping Mox Diamond adds one mana of the chosen color")
    void manaAbilityAddsChosenColor() {
        harness.addToBattlefield(player1, new MoxDiamond());
        Permanent mox = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(mox.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
