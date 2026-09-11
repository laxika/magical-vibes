package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpellbookSeekerCarefulStudyTest extends BaseCardTest {

    @Test
    @DisplayName("Enters prepared with a Careful Study copy")
    void entersPrepared() {
        Permanent seeker = castSeeker();

        assertThat(seeker.isPrepared()).isTrue();
        UUID copyId = seeker.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    @Test
    @DisplayName("Casting Careful Study draws two, discards two, and unprepares Spellbook Seeker")
    void castingPreparedCopyDrawsDiscardsAndUnprepares() {
        Permanent seeker = castSeeker();
        UUID copyId = seeker.getPreparedSpellCardId();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        setDeck(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(seeker.isPrepared()).isFalse();
        assertThat(seeker.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private Permanent castSeeker() {
        harness.setHand(player1, List.of(new SpellbookSeekerCarefulStudy()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Spellbook Seeker");
    }

    private void setDeck(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
