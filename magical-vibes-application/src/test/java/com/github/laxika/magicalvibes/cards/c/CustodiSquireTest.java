package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CustodiSquire.class, GrizzlyBears.class, MindStone.class, RuleOfLaw.class, Forest.class})
class CustodiSquireTest extends BaseCardTest {

    @Test
    void returnsAllCardsTiedForMostVotes() {
        Card creature = new GrizzlyBears();
        Card artifact = new MindStone();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, artifact, land));
        castCustodiSquire();

        PendingInteraction.MultiPermanentChoice firstChoice = activeVote(player1);
        assertThat(firstChoice.validCardIds()).containsExactly(creature.getId(), artifact.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice = activeVote(player2);
        assertThat(secondChoice.validCardIds()).containsExactly(creature.getId(), artifact.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(artifact.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature, artifact);
    }

    @Test
    void returnsOnlyTheCardWithTheMostVotes() {
        Card creature = new GrizzlyBears();
        Card enchantment = new RuleOfLaw();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        castCustodiSquire();

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(enchantment);
    }

    private void castCustodiSquire() {
        harness.setHand(player1, List.of(new CustodiSquire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private PendingInteraction.MultiPermanentChoice activeVote(com.github.laxika.magicalvibes.model.Player player) {
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        return choice;
    }
}
