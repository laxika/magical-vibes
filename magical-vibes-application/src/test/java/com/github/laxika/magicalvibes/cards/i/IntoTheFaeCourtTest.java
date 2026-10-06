package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.s.SnaremasterSprite;
import com.github.laxika.magicalvibes.cards.w.WickedVisitor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntoTheFaeCourt.class, SnaremasterSprite.class, WickedVisitor.class})
class IntoTheFaeCourtTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards and creates a Faerie token")
    void drawsThreeCardsAndCreatesFaerieToken() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        Permanent faerie = castIntoTheFaeCourt();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        assertThat(faerie.getCard().isToken()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(faerie);
        assertThat(faerie.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(faerie.getCard().getSubtypes()).contains(CardSubtype.FAERIE);
        assertThat(gqs.getEffectivePower(gd, faerie)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, faerie)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, faerie, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The Faerie token can block a creature with flying")
    void faerieCanBlockFlyingCreature() {
        Permanent faerie = castIntoTheFaeCourt();
        faerie.setSummoningSick(false);
        addCreatureReady(player2, new SnaremasterSprite()).setAttacking(true);

        prepareDeclareBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(faerie.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The Faerie token cannot block a creature without flying")
    void faerieCannotBlockNonFlyingCreature() {
        Permanent faerie = castIntoTheFaeCourt();
        faerie.setSummoningSick(false);
        addCreatureReady(player2, new WickedVisitor()).setAttacking(true);

        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("The newly created Faerie can block while summoning sick")
    void newlyCreatedFaerieCanBlock() {
        Permanent faerie = castIntoTheFaeCourt();
        addCreatureReady(player2, new SnaremasterSprite()).setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThat(faerie.isSummoningSick()).isTrue();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(faerie.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tapped Faerie cannot block even a flying creature")
    void tappedFaerieCannotBlock() {
        Permanent faerie = castIntoTheFaeCourt();
        faerie.tap();
        addCreatureReady(player2, new SnaremasterSprite()).setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(faerie.isBlocking()).isFalse();
    }

    private Permanent castIntoTheFaeCourt() {
        harness.setHand(player1, List.of(new IntoTheFaeCourt()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
        return findPermanent(player1, "Faerie");
    }
}
