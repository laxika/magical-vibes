package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dreamwinder.class, AvenFlock.class, Forest.class, Island.class})
class DreamwinderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an Island makes the target land an Island until end of turn")
    void sacrificeIslandMakesTargetLandIsland() {
        harness.addToBattlefield(player1, new Dreamwinder());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(island.getCard());
        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        assertThat(forest.getTransientSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("The land type replacement is cleared at end of turn")
    void landTypeReplacementWearsOff() {
        Permanent forest = activateOnForest();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(forest.getTransientLandTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Cannot activate without an Island to sacrifice")
    void cannotActivateWithoutIsland() {
        harness.addToBattlefield(player1, new Dreamwinder());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Dreamwinder());
        harness.addToBattlefield(player1, new Island());
        Permanent nonLandPermanent = harness.addToBattlefieldAndReturn(player1, new AvenFlock());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonLandPermanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Can attack when the defending player controls an Island")
    void canAttackWithDefendingIsland() {
        harness.addToBattlefield(player2, new Island());
        addCreatureReady(player1, new Dreamwinder());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot attack when the defending player controls no Island")
    void cannotAttackWithoutDefendingIsland() {
        addCreatureReady(player1, new Dreamwinder());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only the attacking player controls an Island")
    void cannotAttackWhenOnlyAttackingPlayerControlsIsland() {
        addCreatureReady(player1, new Dreamwinder());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Turning an opponent's land into an Island allows attacking")
    void turningOpponentsLandIntoIslandAllowsAttacking() {
        addCreatureReady(player1, new Dreamwinder());
        harness.addToBattlefield(player1, new Island());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, opponentForest.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private Permanent activateOnForest() {
        harness.addToBattlefield(player1, new Dreamwinder());
        harness.addToBattlefield(player1, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();
        return forest;
    }
}
