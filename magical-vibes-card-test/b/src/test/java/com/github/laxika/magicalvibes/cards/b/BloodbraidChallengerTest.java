package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodbraidChallenger.class, Forest.class, GrizzlyBears.class})
class BloodbraidChallengerTest extends BaseCardTest {

    @Test
    void cascadeOffersFirstCheaperNonlandCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Forest land = new Forest();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, hit));
        harness.castFromHand(player1, new BloodbraidChallenger(), "{3}{R}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class))
                .isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList())
                .containsExactly("Grizzly Bears");

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == hit);
    }

    @Test
    void escapeExilesThreeOtherCardsAndReturnsToBattlefield() {
        BloodbraidChallenger challenger = new BloodbraidChallenger();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Forest third = new Forest();
        harness.setGraveyard(player1, List.of(challenger, first, second, third));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == challenger);
    }

    @Test
    void escapeRequiresThreeOtherCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new BloodbraidChallenger(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void escapingTriggersCascadeAndTheResultingPermanentHasEscaped() {
        BloodbraidChallenger challenger = new BloodbraidChallenger();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(challenger, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(hit));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == hit);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == challenger && permanent.isEscaped());
    }

    @Test
    void cascadeSkipsEqualManaValueAndDecliningReturnsCardsToBottom() {
        BloodbraidChallenger equalManaValue = new BloodbraidChallenger();
        Forest land = new Forest();
        GrizzlyBears hit = new GrizzlyBears();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(equalManaValue, land, hit, untouched));
        harness.castFromHand(player1, new BloodbraidChallenger(), "{3}{R}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(equalManaValue, land, hit);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == hit);
    }

    @Test
    void hasteAllowsAttackingOnTheTurnItEnters() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new BloodbraidChallenger(), "{3}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void escapeCannotExileItselfOrChooseTheSameCardTwice() {
        BloodbraidChallenger challenger = new BloodbraidChallenger();
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setGraveyard(player1, List.of(challenger, first, second, third));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(challenger, first, second, third);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
