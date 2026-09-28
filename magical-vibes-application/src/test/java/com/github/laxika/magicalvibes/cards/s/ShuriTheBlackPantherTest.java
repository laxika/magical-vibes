package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShuriTheBlackPanther.class, GrizzlyBears.class, Spellbook.class, LeoninScimitar.class})
class ShuriTheBlackPantherTest extends BaseCardTest {

    @Test
    void drawsWithThreeArtifactsButDoesNotBoost() {
        Permanent shuri = addCreatureReady(player1, new ShuriTheBlackPanther());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addArtifacts(player1, 3);
        setDeck(new GrizzlyBears());

        attackWith(shuri);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, shuri)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shuri)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void drawsAndBoostsOwnCreaturesWithSixArtifacts() {
        Permanent shuri = addCreatureReady(player1, new ShuriTheBlackPanther());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        addArtifacts(player1, 6);
        setDeck(new GrizzlyBears());

        attackWith(shuri);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, shuri)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shuri)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent shuri = addCreatureReady(player1, new ShuriTheBlackPanther());
        addArtifacts(player1, 6);
        setDeck(new GrizzlyBears());

        attackWith(shuri);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shuri)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shuri)).isEqualTo(3);
    }

    private void addArtifacts(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, i % 2 == 0 ? new Spellbook() : new LeoninScimitar());
        }
    }

    private void attackWith(Permanent creature) {
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();
    }

    private void setDeck(Card card) {
        gd.playerHands.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(card);
    }
}
