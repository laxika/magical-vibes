package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoradinsDisciples.class})
class MoradinsDisciplesTest extends BaseCardTest {

    @Test
    void doubleTeamConjuresACopyWithoutDoubleTeam() {
        Permanent disciples = addCreatureReady(player1, new MoradinsDisciples());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, disciples, Keyword.DOUBLE_TEAM)).isFalse();
        Card copy = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Moradin's Disciples"))
                .findFirst()
                .orElseThrow();
        assertThat(copy.getKeywords()).doesNotContain(Keyword.DOUBLE_TEAM);
    }

    @Test
    void attackTriggerTapsTargetCreatureDefendingPlayerControls() {
        addCreatureReady(player1, new MoradinsDisciples());
        Permanent defender = addCreatureReady(player2, new MoradinsDisciples());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);

        harness.handlePermanentChosen(player1, defender.getId());
        resolveAllTriggers();

        assertThat(defender.isTapped()).isTrue();
    }

    @Test
    void attackTriggerRejectsCreatureIControlAsTarget() {
        addCreatureReady(player1, new MoradinsDisciples());
        Permanent ownCreature = addCreatureReady(player1, new MoradinsDisciples());
        Permanent defender = addCreatureReady(player2, new MoradinsDisciples());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, defender.getId());
        resolveAllTriggers();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(defender.isTapped()).isTrue();
    }

    @Test
    void secondAttackStillTapsButDoesNotConjureAnotherCopy() {
        Permanent disciples = addCreatureReady(player1, new MoradinsDisciples());
        Permanent defender = addCreatureReady(player2, new MoradinsDisciples());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        disciples.untap();
        defender.untap();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        resolveAllTriggers();

        assertThat(defender.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void attackTriggerCanTargetAnAlreadyTappedCreature() {
        addCreatureReady(player1, new MoradinsDisciples());
        Permanent defender = addCreatureReady(player2, new MoradinsDisciples());
        defender.tap();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        resolveAllTriggers();

        assertThat(defender.isTapped()).isTrue();
        harness.assertInHand(player1, "Moradin's Disciples");
    }

    @Test
    void doubleTeamStillResolvesWhenTapTargetLeavesBattlefield() {
        addCreatureReady(player1, new MoradinsDisciples());
        Permanent defender = addCreatureReady(player2, new MoradinsDisciples());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        gd.playerBattlefields.get(player2.getId()).remove(defender);
        gd.playerGraveyards.get(player2.getId()).add(defender.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void playerTwoAttackTapsPlayerOneCreatureAndConjuresForPlayerTwo() {
        addCreatureReady(player2, new MoradinsDisciples());
        Permanent defender = addCreatureReady(player1, new MoradinsDisciples());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player2, defender.getId());
        resolveAllTriggers();

        assertThat(defender.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void conjuredDuplicateRetainsTapAbilityButDoesNotDoubleTeam() {
        addCreatureReady(player1, new MoradinsDisciples());
        harness.setHand(player1, List.of());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of());
        addCreatureReady(player1, duplicate);
        Permanent defender = addCreatureReady(player2, new MoradinsDisciples());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, defender.getId());
        resolveAllTriggers();

        assertThat(defender.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
