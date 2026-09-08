package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Evangelize.class, GrizzlyBears.class})
class EvangelizeTest extends BaseCardTest {

    private void addEvangelizeMana() {
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }

    @Test
    @DisplayName("The caster chooses an opponent, then that opponent chooses the creature")
    void opponentChoosesCreatureTarget() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Evangelize()));
        addEvangelizeMana();

        harness.castSorcery(player1, 0, player2.getId());

        PendingInteraction.PermanentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentChoice.playerId()).isEqualTo(player1.getId());
        assertThat(opponentChoice.validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());

        PendingInteraction.PermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creatureChoice.playerId()).isEqualTo(player2.getId());
        assertThat(creatureChoice.validIds()).containsExactly(opponentCreature.getId());

        harness.handlePermanentChosen(player2, opponentCreature.getId());

        assertThat(gd.stack).singleElement().satisfies(entry ->
                assertThat(entry.getOpponentChosenTargetPlayerId()).isEqualTo(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
    }

    @Test
    @DisplayName("Buyback returns Evangelize after it resolves")
    void buybackReturnsToHand() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Evangelize()));
        addEvangelizeMana();

        harness.castSorceryWithBuyback(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, opponentCreature.getId());

        assertThat(gd.stack).singleElement().extracting(StackEntry::isBuyback).isEqualTo(true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .isInstanceOf(Evangelize.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
    }
}
