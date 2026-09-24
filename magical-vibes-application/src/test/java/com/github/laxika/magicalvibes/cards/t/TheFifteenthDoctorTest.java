package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheFifteenthDoctor.class, GrizzlyBears.class, HowlingMine.class})
class TheFifteenthDoctorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering mills three cards and may return a milled artifact with mana value two or three")
    void enteringMillsAndReturnsMatchingArtifact() {
        Card artifact = new HowlingMine();
        Card nonArtifact = new GrizzlyBears();
        harness.setLibrary(player1, List.of(artifact, nonArtifact, new GrizzlyBears()));
        harness.setHand(player1, List.of(new TheFifteenthDoctor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Howling Mine");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonArtifact);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The first nonartifact spell each turn can use improvise")
    void firstNonartifactSpellGetsImproviseOnly() {
        harness.addToBattlefield(player1, new TheFifteenthDoctor());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(firstArtifact.getId()));
        harness.passBothPriorities();

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
