package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeasonedHallowbladeTest extends BaseCardTest {

    @Test
    void activationRequiresDiscardingACard() {
        addBladeReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0, 1);
    }

    @Test
    void resolvingAbilityDiscardsCardTapsBladeAndGrantsIndestructible() {
        Permanent blade = addBladeReady(player1);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(blade.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, blade, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    void indestructibleResetsAtEndOfTurn() {
        Permanent blade = addBladeReady(player1);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, blade, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, blade, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void cannotActivateWithoutCardToDiscard() {
        addBladeReady(player1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }

    private Permanent addBladeReady(Player player) {
        Permanent perm = new Permanent(new SeasonedHallowblade());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
