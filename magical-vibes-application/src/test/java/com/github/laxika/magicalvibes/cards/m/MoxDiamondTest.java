package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.cards.w.WallOfBlossoms;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoxDiamond.class, VolrathsStronghold.class, WallOfBlossoms.class})
class MoxDiamondTest extends BaseCardTest {

    @Test
    @DisplayName("Entering may discard a land and put Mox Diamond onto the battlefield")
    void entersByDiscardingLand() {
        harness.setHand(player1, List.of(new MoxDiamond(), new VolrathsStronghold(), new WallOfBlossoms()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mox Diamond");
        harness.assertInGraveyard(player1, "Volrath's Stronghold");
        harness.assertInHand(player1, "Wall of Blossoms");
    }

    @Test
    @DisplayName("Declining the land discard puts Mox Diamond into its owner's graveyard")
    void decliningLandDiscardSendsItToGraveyard() {
        harness.setHand(player1, List.of(new MoxDiamond(), new VolrathsStronghold()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Mox Diamond");
        harness.assertInHand(player1, "Volrath's Stronghold");
        harness.assertNotOnBattlefield(player1, "Mox Diamond");
    }

    @Test
    @DisplayName("Without a land in hand Mox Diamond goes straight to the graveyard")
    void noLandSendsItToGraveyardWithoutPrompt() {
        harness.setHand(player1, List.of(new MoxDiamond(), new WallOfBlossoms()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Mox Diamond");
        harness.assertInHand(player1, "Wall of Blossoms");
    }

    @Test
    @DisplayName("A nonland card cannot be chosen for Mox Diamond's entry replacement")
    void cannotChooseNonlandForEntryReplacement() {
        WallOfBlossoms nonland = new WallOfBlossoms();
        VolrathsStronghold land = new VolrathsStronghold();
        harness.setHand(player1, List.of(new MoxDiamond(), nonland, land));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland, land);

        harness.handleCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Mox Diamond");
        harness.assertInGraveyard(player1, "Volrath's Stronghold");
        harness.assertInHand(player1, "Wall of Blossoms");
    }

    @Test
    @DisplayName("Declined entry goes to the card owner's graveyard")
    void declinedEntryUsesCardOwnerGraveyard() {
        MoxDiamond moxDiamond = new MoxDiamond();
        moxDiamond.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(new VolrathsStronghold()));
        harness.setHand(player2, List.of(new WallOfBlossoms()));

        Permanent entering = harness.enterBattlefieldAndReturn(player2, moxDiamond);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(entering);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(moxDiamond);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(moxDiamond);
        harness.assertInHand(player1, "Volrath's Stronghold");
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
