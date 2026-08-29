package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DireDowndraft.class, GrizzlyBears.class, Island.class})
class DireDowndraftTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {2}{U} when targeting a tapped creature")
    void reducedCostWhenTargetingTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        castDireDowndraft(target, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Costs {2}{U} when targeting an attacking creature")
    void reducedCostWhenTargetingAttackingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());

        castDireDowndraft(target, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Requires the full cost when targeting an untapped nonattacking creature")
    void reducedCostDoesNotApplyToUntappedNonattackingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DireDowndraft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The target creature's owner can put it on the bottom of their library")
    void targetOwnerChoosesBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        Card nextCard = new Island();
        setDeck(player2, List.of(topCard, nextCard));

        harness.setHand(player1, List.of(new DireDowndraft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player2.getId());

        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, nextCard, target.getCard());
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new DireDowndraft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDireDowndraft(Permanent target, int genericMana) {
        harness.setHand(player1, List.of(new DireDowndraft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        harness.castInstant(player1, 0, target.getId());
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
