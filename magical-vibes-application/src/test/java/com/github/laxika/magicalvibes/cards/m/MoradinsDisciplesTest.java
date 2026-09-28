package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({MoradinsDisciples.class, GrizzlyBears.class})
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
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

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
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, defender.getId());
        resolveAllTriggers();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(defender.isTapped()).isTrue();
    }
}
