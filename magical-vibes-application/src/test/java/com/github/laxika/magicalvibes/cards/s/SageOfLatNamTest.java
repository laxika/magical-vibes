package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SageOfLatNam.class, JayemdaeTome.class, Spellbook.class})
class SageOfLatNamTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability with one artifact auto-sacrifices it and puts ability on stack")
    void autoSacrificesOnlyArtifact() {
        Permanent sage = addCreatureReady(player1, new SageOfLatNam());
        harness.addToBattlefield(player1, new JayemdaeTome());

        harness.activateAbility(player1, 0, null, null);

        assertThat(sage.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Jayemdae Tome");
        harness.assertInGraveyard(player1, "Jayemdae Tome");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating ability with multiple artifacts asks to choose which to sacrifice")
    void asksForChoiceWithMultipleArtifacts() {
        addCreatureReady(player1, new SageOfLatNam());
        harness.addToBattlefield(player1, new JayemdaeTome());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing an artifact to sacrifice puts ability on stack")
    void choosingArtifactPutsAbilityOnStack() {
        addCreatureReady(player1, new SageOfLatNam());
        Permanent tome = harness.addToBattlefieldAndReturn(player1, new JayemdaeTome());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, tome.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Jayemdae Tome");
        harness.assertOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Draws a card on ability resolution")
    void drawsCardOnResolution() {
        addCreatureReady(player1, new SageOfLatNam());
        harness.addToBattlefield(player1, new JayemdaeTome());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate ability without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        addCreatureReady(player1, new SageOfLatNam());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    @DisplayName("Cannot activate ability when summoning sick (requires tap)")
    void cannotActivateWhenSummoningSick() {
        harness.addToBattlefield(player1, new SageOfLatNam());
        harness.addToBattlefield(player1, new JayemdaeTome());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent sage = addCreatureReady(player1, new SageOfLatNam());
        harness.addToBattlefield(player1, new JayemdaeTome());

        sage.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped artifact can be sacrificed and the card is drawn only on resolution")
    void sacrificesTappedArtifactAndDrawsOnlyOnResolution() {
        Permanent sage = addCreatureReady(player1, new SageOfLatNam());
        Permanent tome = harness.addToBattlefieldAndReturn(player1, new JayemdaeTome());
        tome.tap();
        Spellbook drawnCard = new Spellbook();
        harness.setLibrary(player1, List.of(drawnCard));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(sage.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Jayemdae Tome");
        harness.assertNotOnBattlefield(player1, "Jayemdae Tome");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1).contains(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        Permanent sage = addCreatureReady(player1, new SageOfLatNam());
        harness.addToBattlefield(player2, new JayemdaeTome());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        assertThat(sage.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Jayemdae Tome");
        assertThat(gd.stack).isEmpty();
    }

}
