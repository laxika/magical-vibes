package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CodexShredder.class, Forest.class})
class CodexShredderTest extends BaseCardTest {

    @Test
    void millsOneCardFromTargetPlayersLibrary() {
        harness.addToBattlefield(player1, new CodexShredder());
        Card topCard = new CodexShredder();
        harness.setLibrary(player2, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
    }

    @Test
    void sacrificesItselfAndReturnsTargetCardFromOwnGraveyard() {
        Card returnedCard = new CodexShredder();
        Card remainingCard = new CodexShredder();
        harness.addToBattlefield(player1, new CodexShredder());
        harness.setGraveyard(player1, List.of(returnedCard, remainingCard));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(returnedCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(returnedCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(remainingCard)
                .doesNotContain(returnedCard);
        harness.assertInGraveyard(player1, "Codex Shredder");
        harness.assertNotOnBattlefield(player1, "Codex Shredder");
    }

    @Test
    void cannotTargetCardInOpponentsGraveyard() {
        Card opponentCard = new CodexShredder();
        harness.addToBattlefield(player1, new CodexShredder());
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canMillItselfAndLeavesTheSecondCardInLibrary() {
        Card topCard = new CodexShredder();
        Card secondCard = new Forest();
        harness.addToBattlefield(player1, new CodexShredder());
        harness.setLibrary(player1, List.of(topCard, secondCard));

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void millingAnEmptyLibraryDoesNotCauseALoss() {
        harness.addToBattlefield(player1, new CodexShredder());
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void returnsALandAndPaysSacrificeBeforeResolution() {
        Card source = new CodexShredder();
        Card target = new Forest();
        harness.addToBattlefield(player1, source);
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target, source);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
    }

    @Test
    void cannotPayTheReturnAbilityWithOnlyFourMana() {
        Card target = new Forest();
        harness.addToBattlefield(player1, new CodexShredder());
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Codex Shredder");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    @Test
    void tappedShredderCannotActivateEitherAbility() {
        Card target = new Forest();
        harness.addToBattlefield(player1, new CodexShredder());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Codex Shredder");
    }

    @Test
    void cannotTargetItselfBeforeItIsSacrificed() {
        Card source = new CodexShredder();
        harness.addToBattlefield(player1, source);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(source.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Codex Shredder");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotReturnAnotherCardWhenTargetLeavesGraveyard() {
        Card source = new CodexShredder();
        Card target = new Forest();
        harness.addToBattlefield(player1, source);
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(target);
        harness.setExile(player1, List.of(target));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target, source);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
    }
}
