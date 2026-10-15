package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrambleguardCaptain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PondProphet;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonOfWeaving.class, BrambleguardCaptain.class, ShortBow.class, Plains.class, PondProphet.class})
class SeasonOfWeavingTest extends BaseCardTest {

    @Test
    @DisplayName("Can choose no modes")
    void canChooseNoModes() {
        cast(0);

        assertThat(tokensOf(player1)).isEmpty();
    }

    @Test
    @DisplayName("Can choose the draw mode five times")
    void drawsFiveCards() {
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains(), new Plains(), new Plains()));

        cast(5);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("The copy mode chooses a controlled artifact or creature during resolution")
    void choosesControlledArtifactOrCreatureAtResolution() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new ShortBow());

        cast(6);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(captain.getId(), bow.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ChooseControlledArtifactOrCreatureToCopy.class);

        harness.handlePermanentChosen(player1, bow.getId());

        assertThat(tokensOf(player1)).hasSize(1);
    }

    @Test
    @DisplayName("The same copy mode can be chosen twice")
    void copiesModeTwice() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());

        cast(10);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, captain.getId());

        assertThat(tokensOf(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Returns each nonland nontoken permanent on every battlefield")
    void returnsEachNonlandNontokenPermanent() {
        Permanent ownCaptain = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        Permanent opponentCaptain = harness.addToBattlefieldAndReturn(player2, new BrambleguardCaptain());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Plains());

        cast(6);
        cast(12);

        assertThat(gd.playerHands.get(player1.getId())).contains(ownCaptain.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCaptain.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentLand);
        assertThat(tokensOf(player1)).hasSize(1);
    }

    @Test
    @DisplayName("The copy mode does nothing when the controller controls no artifact or creature")
    void noControlledArtifactOrCreatureDoesNothing() {
        harness.addToBattlefield(player2, new BrambleguardCaptain());

        cast(6);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(tokensOf(player1)).isEmpty();
    }

    @Test
    @DisplayName("Draws before returning nontoken permanents in the same spell")
    void drawsBeforeReturningPermanents() {
        Plains firstDraw = new Plains();
        Plains secondDraw = new Plains();
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        cast(14);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstDraw, secondDraw, captain.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creates the copy before returning nontoken permanents in the same spell")
    void copiesBeforeReturningPermanents() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        Permanent bow = harness.addToBattlefieldAndReturn(player2, new ShortBow());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        cast(15);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(captain.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(bow.getCard());
        assertThat(tokensOf(player1)).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land).doesNotContain(captain);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bow);
    }

    @Test
    @DisplayName("Repeated copy modes can choose different permanents independently")
    void repeatedCopiesCanChooseDifferentPermanents() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new ShortBow());
        Permanent opponentCaptain = harness.addToBattlefieldAndReturn(player2, new BrambleguardCaptain());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        cast(10);

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice.validIds()).containsExactlyInAnyOrder(captain.getId(), bow.getId())
                .doesNotContain(opponentCaptain.getId(), land.getId());
        harness.handlePermanentChosen(player1, captain.getId());

        Permanent firstToken = tokensOf(player1).getFirst();
        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice.validIds())
                .containsExactlyInAnyOrder(captain.getId(), bow.getId(), firstToken.getId());
        harness.handlePermanentChosen(player1, bow.getId());

        assertThat(tokensOf(player1)).extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder(captain.getCard().getName(), bow.getCard().getName());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A repeated copy mode can copy the token created by its previous instance")
    void canCopyTheNewToken() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());

        cast(10);

        Permanent firstToken = tokensOf(player1).getFirst();
        harness.handlePermanentChosen(player1, firstToken.getId());

        assertThat(tokensOf(player1)).hasSize(2)
                .allSatisfy(token -> assertThat(token.getCard().getName())
                        .isEqualTo(captain.getCard().getName()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Copying does not copy tapped state or counters")
    void doesNotCopyTappedStateOrCounters() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        captain.tap();
        captain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        cast(6);

        assertThat(tokensOf(player1)).singleElement().satisfies(token -> {
            assertThat(token.isTapped()).isFalse();
            assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        });
        assertThat(captain.isTapped()).isTrue();
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("A copied enter ability waits until all spell modes have resolved")
    void copiedEnterAbilityResolvesAfterTheReturnMode() {
        PondProphet prophet = new PondProphet();
        Plains drawnCard = new Plains();
        harness.addToBattlefield(player1, prophet);
        harness.setLibrary(player1, List.of(drawnCard));

        cast(15);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(prophet);
        assertThat(tokensOf(player1)).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(prophet, drawnCard);
    }

    @Test
    @DisplayName("Draw and repeated copy modes can use all five pawprints together")
    void drawsThenCopiesTwice() {
        Plains drawnCard = new Plains();
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        harness.setLibrary(player1, List.of(drawnCard));

        cast(11);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.handlePermanentChosen(player1, captain.getId());

        assertThat(tokensOf(player1)).hasSize(2);
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(new SeasonOfWeaving()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, modeIndex);
    }

    private List<Permanent> tokensOf(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
