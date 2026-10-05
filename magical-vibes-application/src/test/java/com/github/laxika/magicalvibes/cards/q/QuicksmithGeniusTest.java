package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuicksmithGenius.class, GlazeFiend.class, GrizzlyBears.class, Forest.class})
class QuicksmithGeniusTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact entering under your control lets you discard a card to draw a card")
    void acceptTriggerDiscardsThenDraws() {
        GrizzlyBears discarded = new GrizzlyBears();
        GlazeFiend artifact = new GlazeFiend();
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new QuicksmithGenius());
        harness.setHand(player1, new ArrayList<>(List.of(discarded, artifact)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Declining the trigger neither discards nor draws")
    void declineTriggerDoesNothing() {
        GrizzlyBears discarded = new GrizzlyBears();
        GlazeFiend artifact = new GlazeFiend();
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new QuicksmithGenius());
        harness.setHand(player1, new ArrayList<>(List.of(discarded, artifact)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("An artifact entering under an opponent's control does not trigger")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new QuicksmithGenius());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Accepting with an empty hand cannot draw a card")
    void emptyHandDoesNotDraw() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new QuicksmithGenius());
        harness.castFromHand(player1, new GlazeFiend(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A nonartifact creature entering under your control does not trigger")
    void nonartifactCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new QuicksmithGenius());
        harness.castFromHand(player1, new QuicksmithGenius(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
