package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElrondOfTheWhiteCouncil.class, GrizzlyBears.class})
class ElrondOfTheWhiteCouncilTest extends BaseCardTest {

    @Test
    void fellowshipStealsChosenCreatureAndAidCountersAllControlledCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castElrond();

        harness.handleListChoice(player1, ChoiceContext.ElrondOfTheWhiteCouncilChoice.AID);
        harness.handleListChoice(player2, ChoiceContext.ElrondOfTheWhiteCouncilChoice.FELLOWSHIP);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                firstOpponentCreature.getId(), secondOpponentCreature.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(firstOpponentCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstOpponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondOpponentCreature);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(firstOpponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(als.canAttackDefender(gd, firstOpponentCreature, player2.getId())).isFalse();
    }

    private void castElrond() {
        harness.setHand(player1, List.of(new ElrondOfTheWhiteCouncil()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.ColorChoice vote =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(vote).isNotNull();
        assertThat(vote.options()).containsExactlyElementsOf(
                ChoiceContext.ElrondOfTheWhiteCouncilChoice.OPTIONS);
    }
}
