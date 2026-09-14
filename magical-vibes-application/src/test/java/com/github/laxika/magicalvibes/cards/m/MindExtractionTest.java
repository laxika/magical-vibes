package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BloodfireDwarf;
import com.github.laxika.magicalvibes.cards.c.CetaDisciple;
import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.e.EbonyTreefolk;
import com.github.laxika.magicalvibes.cards.g.GladeGnarr;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindExtraction.class, BloodfireDwarf.class, CetaDisciple.class, Dodecapod.class,
        EbonyTreefolk.class, GladeGnarr.class, MournfulZombie.class, YavimayaCoast.class})
class MindExtractionTest extends BaseCardTest {

    @Test
    void discardsCardsSharingAnyColorWithSacrificedMulticoloredCreature() {
        Permanent sacrificed = addCreatureReady(player1, new EbonyTreefolk());
        Card greenCard = new GladeGnarr();
        Card blackCard = new MournfulZombie();
        Card multicoloredCard = new EbonyTreefolk();
        Card blueCard = new CetaDisciple();
        Card land = new YavimayaCoast();
        harness.setHand(player1, List.of(new MindExtraction()));
        harness.setHand(player2, List.of(greenCard, blackCard, multicoloredCard, blueCard, land));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(blueCard, land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(greenCard, blackCard,
                multicoloredCard);
        assertThat(gameLogContains("reveals their hand")).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
    }

    @Test
    void colorlessSacrificedCreatureRevealsButDiscardsNothing() {
        Permanent sacrificed = addCreatureReady(player1, new Dodecapod());
        Card greenCard = new GladeGnarr();
        Card redCard = new BloodfireDwarf();
        harness.setHand(player1, List.of(new MindExtraction()));
        harness.setHand(player2, List.of(greenCard, redCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(greenCard, redCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void canTargetCasterAndDiscardMatchingCardsFromTheirOwnHand() {
        Permanent sacrificed = addCreatureReady(player1, new BloodfireDwarf());
        Card spell = new MindExtraction();
        Card redCard = new BloodfireDwarf();
        Card blueCard = new CetaDisciple();
        harness.setHand(player1, List.of(spell, redCard, blueCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, player1.getId(), sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blueCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(sacrificed.getCard(), spell, redCard);
    }

    @Test
    void cannotCastWithoutACreatureToSacrifice() {
        harness.setHand(player1, List.of(new MindExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
