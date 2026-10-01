package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RanklePitilessTrickster.class, GrizzlyBears.class})
class RanklePitilessTricksterTest extends BaseCardTest {

    @Test
    @DisplayName("Has haste and lifelink while an opponent controls no creatures")
    void hasConditionalKeywords() {
        Permanent rankle = addReadyRankle();

        assertThat(gqs.hasKeyword(gd, rankle, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, rankle, Keyword.LIFELINK)).isTrue();

        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, rankle, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, rankle, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Paying life on entry makes each player discard and sacrifice")
    void paysLifeToDiscardAndSacrifice() {
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card player1Card = new GrizzlyBears();
        Card player2Card = new GrizzlyBears();
        harness.setHand(player1, List.of(new RanklePitilessTrickster(), player1Card));
        harness.setHand(player2, List.of(player2Card));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(player2Creature);
        assertThat(rankle.getCard().getPower()).isEqualTo(3);
    }

    private Permanent addReadyRankle() {
        Permanent rankle = harness.addToBattlefieldAndReturn(player1, new RanklePitilessTrickster());
        rankle.setSummoningSick(false);
        return rankle;
    }
}
