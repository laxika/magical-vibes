package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({DimensionXPizzasaur.class, GrizzlyBears.class, HillGiant.class, Pacifism.class})
class DimensionXPizzasaurTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts two counters on a creature, then destroys an eligible creature")
    void etbCountersThenDestroysWithinCounterLimit() {
        Permanent counterTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent eligibleTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ineligibleTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(counterTarget);
        assertThat(counterTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(eligibleTarget.getId())
                .doesNotContain(ineligibleTarget.getId());

        harness.handlePermanentChosen(player1, eligibleTarget.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ineligibleTarget);
    }

    @Test
    @DisplayName("The reflexive destruction is optional when no creature is eligible")
    void etbMayDestroyNothing() {
        Permanent counterTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(counterTarget);
        assertThat(counterTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.handlePermanentChosen(player1, player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(counterTarget);
    }

    @Test
    @DisplayName("All counter types contribute to the destruction limit")
    void countsOtherCounterTypesAlongsidePlusOneCounters() {
        Permanent counterTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        counterTarget.setCounterCount(CounterType.STUN, 2);
        Permanent destructionTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(counterTarget);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(destructionTarget.getId());
        harness.handlePermanentChosen(player1, destructionTarget.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Losing counters in response makes the destruction target illegal")
    void rechecksCounterTotalWhenDestructionResolves() {
        Permanent counterTarget = addReadyPizzasaur();
        Permanent destructionTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(counterTarget);
        harness.handlePermanentChosen(player1, destructionTarget.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destruction can be declined even when eligible creatures exist")
    void mayDeclineEligibleDestructionTarget() {
        Permanent counterTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(counterTarget);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counters on opponents' permanents do not increase the limit")
    void excludesOpponentsCounters() {
        Permanent counterTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        cast(counterTarget);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
    }

    @Test
    @DisplayName("A summoning-sick Pizzasaur cannot pay the tap cost")
    void sacrificeAbilityRequiresNoSummoningSickness() {
        harness.addToBattlefield(player1, new DimensionXPizzasaur());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Dimension X Pizzasaur");
        harness.assertNotInGraveyard(player1, "Dimension X Pizzasaur");
    }

    @Test
    @DisplayName("Sacrifice ability gains 3 life and makes each opponent lose 3 life")
    void sacrificeAbilityDrainsOpponent() {
        Permanent pizzasaur = addReadyPizzasaur();
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Dimension X Pizzasaur");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pizzasaur);
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void etbRejectsNoncreatureTarget() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        harness.setHand(player1, List.of(new DimensionXPizzasaur()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new DimensionXPizzasaur()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyPizzasaur() {
        Permanent pizzasaur = harness.addToBattlefieldAndReturn(player1, new DimensionXPizzasaur());
        pizzasaur.setSummoningSick(false);
        return pizzasaur;
    }
}
