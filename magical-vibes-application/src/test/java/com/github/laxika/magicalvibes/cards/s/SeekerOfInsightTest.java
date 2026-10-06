package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LuxaRiverShrine;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeekerOfInsight.class, Cancel.class, Forest.class, DuneBeetle.class, LuxaRiverShrine.class})
class SeekerOfInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate when no spell has been cast this turn")
    void cannotActivateWithoutNoncreatureSpell() {
        addReadySeeker(player1);
        harness.setHand(player1, List.of(new DuneBeetle()));
        harness.setLibrary(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature spell");
    }

    @Test
    @DisplayName("Casting a creature spell does not enable the ability")
    void creatureSpellDoesNotEnableActivation() {
        addReadySeeker(player1);
        gd.recordSpellCast(player1.getId(), new DuneBeetle());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature spell");
    }

    @Test
    @DisplayName("Can activate after casting a noncreature spell this turn")
    void canActivateAfterNoncreatureSpell() {
        addReadySeeker(player1);
        gd.recordSpellCast(player1.getId(), new Cancel());
        harness.setHand(player1, List.of(new DuneBeetle()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving loots: draws a card then discards one to the graveyard")
    void fullLootCycleAfterNoncreatureSpell() {
        addReadySeeker(player1);
        gd.recordSpellCast(player1.getId(), new Cancel());
        DuneBeetle beetle = new DuneBeetle();
        harness.setHand(player1, List.of(beetle));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Drew the Forest — hand is now [Dune Beetle, Forest] and awaiting a discard choice.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        // Discard the Dune Beetle at index 0.
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        harness.assertInGraveyard(player1, "Dune Beetle");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A noncreature permanent spell enables activation before it resolves")
    void canActivateWhileArtifactSpellIsOnStack() {
        Permanent seeker = addReadySeeker(player1);
        harness.castFromHand(player1, new LuxaRiverShrine(), "{3}");
        harness.setHand(player1, List.of(new DuneBeetle()));
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);

        assertThat(seeker.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Dune Beetle");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Luxa River Shrine");
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not enable activation")
    void opponentsSpellDoesNotEnableActivation() {
        Permanent seeker = addReadySeeker(player1);
        gd.recordSpellCast(player2.getId(), new Cancel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature spell");
        assertThat(seeker.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Seeker cannot pay its tap cost")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new SeekerOfInsight());
        gd.recordSpellCast(player1.getId(), new Cancel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Seeker cannot activate again")
    void tappedSeekerCannotActivate() {
        Permanent seeker = addReadySeeker(player1);
        seeker.setTapped(true);
        gd.recordSpellCast(player1.getId(), new Cancel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With an empty hand, the drawn card must be discarded")
    void emptyHandStillDrawsThenDiscards() {
        addReadySeeker(player1);
        gd.recordSpellCast(player1.getId(), new Cancel());
        harness.setHand(player1, List.of());
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySeeker(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SeekerOfInsight());
        perm.setSummoningSick(false);
        return perm;
    }
}
