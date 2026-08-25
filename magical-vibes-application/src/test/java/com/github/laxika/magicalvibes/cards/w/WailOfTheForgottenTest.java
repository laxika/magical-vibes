package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WailOfTheForgotten.class, GrizzlyBears.class, Plains.class, Shock.class})
class WailOfTheForgottenTest extends BaseCardTest {

    @Test
    void returnsTargetNonlandPermanentToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWail(0, List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void targetOpponentDiscardsACard() {
        Card discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        castWail(1, List.of(player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(discarded);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    void looksAtTopThreeAndPutsOneIntoHandAndTheRestIntoGraveyard() {
        Card first = new Shock();
        Card chosen = new GrizzlyBears();
        Card third = new Plains();
        harness.setLibrary(player1, List.of(first, chosen, third));
        harness.setHand(player1, List.of(new WailOfTheForgotten()));
        addWailMana();

        harness.castModalSorcery(player1, 0, 2, List.of());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardsChosen(List.of(chosen.getId())));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void allModesAreAvailableWithEightPermanentCardsInTheGraveyard() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(new WailOfTheForgotten()));
        addWailMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 1},
                List.of(target.getId(), player2.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(discarded);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    void cannotChooseMultipleModesBeforeDescendEight() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new WailOfTheForgotten()));
        addWailMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{0, 1}, List.of(target.getId(), player2.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetALandWithTheReturnMode() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new WailOfTheForgotten()));
        addWailMana();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    private void castWail(int modeIndex, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new WailOfTheForgotten()));
        addWailMana();
        harness.castModalSorcery(player1, 0, modeIndex, targetIds);
        harness.passBothPriorities();
    }

    private void addWailMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
