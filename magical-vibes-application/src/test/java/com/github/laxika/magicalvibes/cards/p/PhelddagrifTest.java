package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
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

class PhelddagrifTest extends BaseCardTest {

    private Permanent addPhelddagrif(ManaColor mana) {
        Permanent hippo = addCreatureReady(player1, new Phelddagrif());
        harness.addMana(player1, mana, 1);
        return hippo;
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    @Test
    @DisplayName("{G} grants trample and gives the opponent a 1/1 Hippo token")
    void greenAbilityGrantsTrampleAndGivesHippo() {
        Permanent phelddagrif = addPhelddagrif(ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(phelddagrif.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> "Hippo".equals(p.getCard().getName()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> "Hippo".equals(p.getCard().getName()))
                .singleElement()
                .satisfies(token -> {
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Trample from {G} wears off at end of turn")
    void trampleWearsOff() {
        Permanent phelddagrif = addPhelddagrif(ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(phelddagrif.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("{W} grants flying and gives the opponent 2 life")
    void whiteAbilityGrantsFlyingAndGivesLife() {
        Permanent phelddagrif = addPhelddagrif(ManaColor.WHITE);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(phelddagrif.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("{U} bounces Phelddagrif and offers the opponent a card")
    void blueAbilityBouncesAndOffersDraw() {
        addPhelddagrif(ManaColor.BLUE);
        setDeck(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> "Phelddagrif".equals(p.getCard().getName()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> "Phelddagrif".equals(c.getName()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("The opponent may decline the {U} draw")
    void opponentMayDeclineDraw() {
        addPhelddagrif(ManaColor.BLUE);
        setDeck(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore);
    }
}
