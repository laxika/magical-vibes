package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.w.WickerfolkThresher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrialOfAgony.class, WickerfolkThresher.class})
class TrialOfAgonyTest extends BaseCardTest {

    @Test
    @DisplayName("The targeted creatures' controller chooses which creature receives the damage")
    void opponentChoosesCreatureToDamage() {
        Permanent first = addToughWickerfolkThresher();
        Permanent second = addToughWickerfolkThresher();

        castTrialOfAgony(first, second);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    @DisplayName("The chosen creature takes 5 damage and the other can't block")
    void damagesChosenAndRestrictsOther() {
        Permanent chosen = addToughWickerfolkThresher();
        Permanent other = addToughWickerfolkThresher();

        castTrialOfAgony(chosen, other);
        harness.handlePermanentChosen(player2, chosen.getId());

        assertThat(chosen.getMarkedDamage()).isEqualTo(5);
        assertThat(chosen.isCantBlockThisTurn()).isFalse();
        assertThat(other.getMarkedDamage()).isZero();
        assertThat(other.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("If one target is illegal, the remaining creature receives the damage")
    void damagesRemainingLegalTarget() {
        Permanent remaining = addToughWickerfolkThresher();
        Permanent removed = addToughWickerfolkThresher();

        harness.setHand(player1, List.of(new TrialOfAgony()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, List.of(remaining.getId(), removed.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(remaining.getMarkedDamage()).isEqualTo(5);
        assertThat(remaining.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The spell can't target a creature controlled by its caster")
    void cannotTargetOwnCreature() {
        Permanent own = addToughWickerfolkThresherFor(player1);
        Permanent opponent = addToughWickerfolkThresher();

        harness.setHand(player1, List.of(new TrialOfAgony()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(own.getId(), opponent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentCanChooseSecondTarget() {
        Permanent first = addToughWickerfolkThresher();
        Permanent second = addToughWickerfolkThresher();

        castTrialOfAgony(first, second);
        harness.handlePermanentChosen(player2, second.getId());

        assertThat(second.getMarkedDamage()).isEqualTo(5);
        assertThat(second.isCantBlockThisTurn()).isFalse();
        assertThat(first.getMarkedDamage()).isZero();
        assertThat(first.isCantBlockThisTurn()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotChooseSameCreatureTwice() {
        Permanent creature = addToughWickerfolkThresher();
        harness.setHand(player1, List.of(new TrialOfAgony()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void noEffectWhenBothTargetsLeaveBattlefield() {
        Permanent first = addToughWickerfolkThresher();
        Permanent second = addToughWickerfolkThresher();
        harness.setHand(player1, List.of(new TrialOfAgony()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(first, second));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
        assertThat(first.isCantBlockThisTurn()).isFalse();
        assertThat(second.isCantBlockThisTurn()).isFalse();
        harness.assertInGraveyard(player1, "Trial of Agony");
    }

    @Test
    void remainingSecondTargetTakesDamageWhenFirstLeaves() {
        Permanent removed = addToughWickerfolkThresher();
        Permanent remaining = addToughWickerfolkThresher();
        harness.setHand(player1, List.of(new TrialOfAgony()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, List.of(removed.getId(), remaining.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removed);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(remaining.getMarkedDamage()).isEqualTo(5);
        assertThat(remaining.isCantBlockThisTurn()).isFalse();
    }

    private void castTrialOfAgony(Permanent first, Permanent second) {
        harness.setHand(player1, List.of(new TrialOfAgony()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));
    }

    private Permanent addToughWickerfolkThresher() {
        return addToughWickerfolkThresherFor(player2);
    }

    private Permanent addToughWickerfolkThresherFor(Player player) {
        Permanent creature = addCreatureReady(player, new WickerfolkThresher());
        TestCards.mutableCard(creature).setToughness(6);
        return creature;
    }
}
