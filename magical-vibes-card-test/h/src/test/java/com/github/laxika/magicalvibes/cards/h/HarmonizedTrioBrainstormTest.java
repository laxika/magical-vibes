package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarmonizedTrioBrainstorm.class})
class HarmonizedTrioBrainstormTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping two untapped creatures prepares Harmonized Trio")
    void tappingTwoCreaturesPreparesTrio() {
        Permanent trio = addReady(new HarmonizedTrioBrainstorm());
        Permanent other = addReady(new HarmonizedTrioBrainstorm());
        Permanent third = addReady(new HarmonizedTrioBrainstorm());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(trio.isPrepared()).isTrue();
        assertThat(trio.getPreparedSpellCardId()).isNotNull();
        assertThat(trio.isTapped()).isTrue();
        assertThat(other.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting prepared Brainstorm unprepares Harmonized Trio")
    void castingPreparedBrainstormUnpreparesTrio() {
        Permanent trio = addReady(new HarmonizedTrioBrainstorm());
        addReady(new HarmonizedTrioBrainstorm());
        addReady(new HarmonizedTrioBrainstorm());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        UUID spellId = trio.getPreparedSpellCardId();
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            library.add(new HarmonizedTrioBrainstorm());
        }
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, spellId);
        harness.passBothPriorities();

        assertThat(trio.isPrepared()).isFalse();
        assertThat(trio.getPreparedSpellCardId()).isNull();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
    }

    private Permanent addReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    void cannotUseSourceAsOneOfTwoAdditionalCreatures() {
        Permanent trio = addReady(new HarmonizedTrioBrainstorm());
        Permanent other = addReady(new HarmonizedTrioBrainstorm());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(trio.isTapped()).isFalse();
        assertThat(other.isTapped()).isFalse();
        assertThat(trio.isPrepared()).isFalse();
    }

    @Test
    void summoningSickSupportCreaturesCanPayAdditionalTapCost() {
        Permanent trio = addReady(new HarmonizedTrioBrainstorm());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HarmonizedTrioBrainstorm());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new HarmonizedTrioBrainstorm());
        second.setSummoningSick(true);
        third.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        assertThat(trio.isPrepared()).isFalse();
        assertThat(trio.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(trio.isPrepared()).isTrue();
    }

    @Test
    void summoningSickTrioCannotActivateTapAbility() {
        Permanent trio = addReady(new HarmonizedTrioBrainstorm());
        trio.setSummoningSick(true);
        addReady(new HarmonizedTrioBrainstorm());
        addReady(new HarmonizedTrioBrainstorm());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trio.isPrepared()).isFalse();
        assertThat(trio.isTapped()).isFalse();
    }

    @Test
    void preparedBrainstormReturnsCardsInChosenOrderAndRejectsDuplicateSelection() {
        Permanent trio = addReady(new HarmonizedTrioBrainstorm());
        addReady(new HarmonizedTrioBrainstorm());
        addReady(new HarmonizedTrioBrainstorm());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        UUID spellId = trio.getPreparedSpellCardId();
        List<Card> library = List.of(new HarmonizedTrioBrainstorm(), new HarmonizedTrioBrainstorm(),
                new HarmonizedTrioBrainstorm(), new HarmonizedTrioBrainstorm());
        Card alreadyInHand = new HarmonizedTrioBrainstorm();
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(alreadyInHand));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromExile(player1, spellId);
        assertThat(trio.isPrepared()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(alreadyInHand, library.get(0), library.get(1), library.get(2));
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(alreadyInHand.getId(), alreadyInHand.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(alreadyInHand, library.get(0), library.get(1), library.get(2));

        harness.handleMultipleCardsChosen(player1, List.of(library.get(2).getId(), alreadyInHand.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0), library.get(1));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(2), alreadyInHand, library.get(3));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
