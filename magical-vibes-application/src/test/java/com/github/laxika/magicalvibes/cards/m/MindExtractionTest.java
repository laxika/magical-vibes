package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YavimayaKavu;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindExtraction.class, Counterspell.class, Forest.class, GiantGrowth.class,
        PhyrexianWalker.class, Shock.class, YavimayaKavu.class})
class MindExtractionTest extends BaseCardTest {

    @Test
    void discardsCardsSharingAnyColorWithSacrificedMulticoloredCreature() {
        Permanent sacrificed = addCreatureReady(player1, new YavimayaKavu());
        Card greenCard = new GiantGrowth();
        Card redCard = new Shock();
        Card multicoloredCard = new YavimayaKavu();
        Card blueCard = new Counterspell();
        Card land = new Forest();
        harness.setHand(player1, List.of(new MindExtraction()));
        harness.setHand(player2, new ArrayList<>(List.of(greenCard, redCard, multicoloredCard, blueCard, land)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(blueCard, land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(greenCard, redCard,
                multicoloredCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
    }

    @Test
    void colorlessSacrificedCreatureRevealsButDiscardsNothing() {
        Permanent sacrificed = addCreatureReady(player1, new PhyrexianWalker());
        Card greenCard = new GiantGrowth();
        Card redCard = new Shock();
        harness.setHand(player1, List.of(new MindExtraction()));
        harness.setHand(player2, new ArrayList<>(List.of(greenCard, redCard)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(greenCard, redCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
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
