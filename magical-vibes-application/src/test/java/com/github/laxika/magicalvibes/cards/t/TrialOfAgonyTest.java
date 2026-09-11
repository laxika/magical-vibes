package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
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

@CardUsed({TrialOfAgony.class, AirElemental.class})
class TrialOfAgonyTest extends BaseCardTest {

    @Test
    @DisplayName("The targeted creatures' controller chooses which creature receives the damage")
    void opponentChoosesCreatureToDamage() {
        Permanent first = addToughAirElemental();
        Permanent second = addToughAirElemental();

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
        Permanent chosen = addToughAirElemental();
        Permanent other = addToughAirElemental();

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
        Permanent remaining = addToughAirElemental();
        Permanent removed = addToughAirElemental();

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
        Permanent own = addToughAirElementalFor(player1);
        Permanent opponent = addToughAirElemental();

        harness.setHand(player1, List.of(new TrialOfAgony()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(own.getId(), opponent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTrialOfAgony(Permanent first, Permanent second) {
        harness.setHand(player1, List.of(new TrialOfAgony()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
    }

    private Permanent addToughAirElemental() {
        return addToughAirElementalFor(player2);
    }

    private Permanent addToughAirElementalFor(Player player) {
        Permanent creature = addCreatureReady(player, new AirElemental());
        TestCards.mutableCard(creature).setToughness(6);
        return creature;
    }
}
