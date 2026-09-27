package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChooseYourDemise.class, Forest.class, Island.class, Mountain.class, Plains.class})
class ChooseYourDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("The controller separates four cards and the opponent chooses the hand pile")
    void opponentChoosesPileForHandAndOtherPileGoesToBottom() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card plains = new Plains();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(island, forest, mountain, plains));

        resolveScheme();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.description()).contains(island.getName(), forest.getName())
                .contains("2 cards");

        harness.handleMayAbilityChosen(player2, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mountain, plains);
    }

    @Test
    @DisplayName("The face-down pile remains hidden while the opponent chooses it")
    void faceDownPileCanBeChosenForHand() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card plains = new Plains();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(island, forest, mountain, plains));

        resolveScheme();
        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.description()).contains(island.getName(), forest.getName())
                .doesNotContain(mountain.getName(), plains.getName());

        harness.handleMayAbilityChosen(player2, false);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(mountain, plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, forest);
    }

    private void resolveScheme() {
        ChooseYourDemise scheme = new ChooseYourDemise();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }
}
