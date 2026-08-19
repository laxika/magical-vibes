package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuestingPhelddagrifTest extends BaseCardTest {

    private Permanent addQuestingPhelddagrif(ManaColor mana) {
        Permanent phelddagrif = addCreatureReady(player1, new QuestingPhelddagrif());
        harness.addMana(player1, mana, 1);
        return phelddagrif;
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    @Test
    @DisplayName("{G} gets +1/+1 and gives the opponent a 1/1 Hippo token")
    void greenAbilityBoostsAndGivesHippo() {
        Permanent phelddagrif = addQuestingPhelddagrif(ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, phelddagrif)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, phelddagrif)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> "Hippo".equals(p.getCard().getName()))
                .singleElement()
                .satisfies(token -> {
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("The green ability's boost wears off at end of turn")
    void greenAbilityBoostWearsOff() {
        Permanent phelddagrif = addQuestingPhelddagrif(ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, phelddagrif)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, phelddagrif)).isEqualTo(4);
    }

    @Test
    @DisplayName("{W} grants protection from black and red and gives the opponent 2 life")
    void whiteAbilityGrantsProtectionAndLife() {
        Permanent phelddagrif = addQuestingPhelddagrif(ManaColor.WHITE);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(phelddagrif.getProtectionFromColorsUntilEndOfTurn())
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("{U} grants flying and offers the opponent a card")
    void blueAbilityGrantsFlyingAndOffersDraw() {
        Permanent phelddagrif = addQuestingPhelddagrif(ManaColor.BLUE);
        setDeck(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(phelddagrif.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("The opponent may decline the {U} draw")
    void opponentMayDeclineDraw() {
        addQuestingPhelddagrif(ManaColor.BLUE);
        setDeck(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore);
    }
}
