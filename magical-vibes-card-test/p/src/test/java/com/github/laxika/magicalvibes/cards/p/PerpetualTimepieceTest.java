package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PerpetualTimepiece.class, Forest.class, GrizzlyBears.class, AirElemental.class, PsychogenicProbe.class})
class PerpetualTimepieceTest extends BaseCardTest {

    @Test
    void millsTwoCards() {
        addTimepiece();
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new AirElemental();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void exilesItselfAndShufflesTargetedGraveyardCardsIntoLibrary() {
        Permanent timepiece = addTimepiece();
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card libraryCard = new AirElemental();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(libraryCard.getId(), first.getId(), second.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(timepiece.getCard());
    }

    @Test
    void canChooseNoGraveyardTargets() {
        Permanent timepiece = addTimepiece();
        Card card = new Forest();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(timepiece.getCard());
    }

    @Test
    void cannotTargetAnOpponentsGraveyard() {
        addTimepiece();
        Card card = new Forest();
        harness.setGraveyard(player2, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(card.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void millsOnlyRemainingCardWithoutDrawingFromEmptyLibrary() {
        addTimepiece();
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void cannotMillAgainWhileTapped() {
        Permanent timepiece = addTimepiece();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(timepiece.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
    }

    @Test
    void canExileTappedTimepieceWhileItsMillAbilityIsPending() {
        Permanent timepiece = addTimepiece();
        Card graveyardCard = new Forest();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(graveyardCard.getId()));

        harness.assertNotOnBattlefield(player1, "Perpetual Timepiece");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(timepiece.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);

        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(graveyardCard);

        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void choosingZeroTargetsStillShufflesLibrary() {
        addTimepiece();
        harness.addToBattlefield(player2, new PsychogenicProbe());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void choosingZeroTargetsStillShufflesEmptyLibrary() {
        addTimepiece();
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void shufflesRemainingLegalTargetWhenAnotherTargetLeavesGraveyard() {
        Permanent timepiece = addTimepiece();
        Card removed = new Forest();
        Card remaining = new Forest();
        harness.setGraveyard(player1, List.of(removed, remaining));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.setExile(player1, List.of(timepiece.getCard(), removed));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(removed);
    }

    @Test
    void doesNotShuffleWhenAllChosenTargetsBecomeIllegal() {
        Permanent timepiece = addTimepiece();
        harness.addToBattlefield(player2, new PsychogenicProbe());
        Card target = new Forest();
        Card libraryCard = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(timepiece.getCard(), target));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    private Permanent addTimepiece() {
        return harness.addToBattlefieldAndReturn(player1, new PerpetualTimepiece());
    }
}
