package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrixhavenSkycoachTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may search for a basic land and put it into hand")
    void etbMaySearchForBasicLand() {
        Plains plains = new Plains();
        Forest forest = new Forest();
        setupLibrary(List.of(plains, forest, new GrizzlyBears()));
        harness.setHand(player1, List.of(new StrixhavenSkycoach()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(plains, forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB search may be declined")
    void etbSearchMayBeDeclined() {
        setupLibrary(List.of(new Plains(), new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new StrixhavenSkycoach()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Crew 2 animates Strixhaven Skycoach")
    void crewWithSufficientPower() {
        Permanent skycoach = addSkycoachReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skycoach.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, skycoach)).isTrue();
        assertThat(crew.isTapped()).isTrue();
        assertThat(skycoach.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Crew animation resets at end of turn")
    void crewResetsAtEndOfTurn() {
        Permanent skycoach = addSkycoachReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, skycoach)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skycoach.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, skycoach)).isFalse();
    }

    @Test
    @DisplayName("Cannot crew without enough creature power")
    void cannotCrewWithoutEnoughPower() {
        addSkycoachReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    private Permanent addSkycoachReady(Player player) {
        Permanent permanent = new Permanent(new StrixhavenSkycoach());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void setupLibrary(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
