package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EntrapmentManeuver.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class EntrapmentManeuverTest extends BaseCardTest {

    private void castAtPlayer2() {
        harness.setHand(player1, List.of(new EntrapmentManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Target player sacrifices their only attacking creature and the caster creates tokens equal to its toughness")
    void sacrificesAttackerAndCasterCreatesTokens() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setAttacking(true);

        castAtPlayer2();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        assertThat(countPermanents(player2, "Soldier")).isZero();
    }

    @Test
    @DisplayName("The target player chooses among attacking creatures, while nonattacking creatures are not eligible")
    void choosesOnlyAmongAttackingCreatures() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setAttacking(true);
        Permanent giant = addCreatureReady(player2, new HillGiant());
        giant.setAttacking(true);
        Permanent nonattacker = addCreatureReady(player2, new GrizzlyBears());

        castAtPlayer2();

        PendingInteraction.PermanentChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), giant.getId());
        harness.handlePermanentChosen(player2, giant.getId());

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .contains(nonattacker);
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(3);
    }

    @Test
    @DisplayName("Without an attacking creature, the target player keeps their creatures and no tokens are created")
    void noAttackerDoesNothing() {
        addCreatureReady(player2, new GrizzlyBears());

        castAtPlayer2();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Soldier")).isZero();
    }

    @Test
    @DisplayName("The spell cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new EntrapmentManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
