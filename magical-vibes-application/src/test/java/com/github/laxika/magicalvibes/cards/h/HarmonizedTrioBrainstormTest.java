package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HarmonizedTrioBrainstormTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping two untapped creatures prepares Harmonized Trio")
    void tappingTwoCreaturesPreparesTrio() {
        Permanent trio = addReady(new HarmonizedTrioBrainstorm());
        Permanent other = addReady(new GrizzlyBears());
        Permanent third = addReady(new GrizzlyBears());

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
        addReady(new GrizzlyBears());
        addReady(new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        UUID spellId = trio.getPreparedSpellCardId();
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            library.add(new GrizzlyBears());
        }
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(library);
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
}
