package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EzzarootChanneler.class, GrizzlyBears.class, Divination.class})
class EzzarootChannelerTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells cost less by the amount of life gained this turn")
    void reducesCreatureSpellCostByLifeGained() {
        harness.addToBattlefield(player1, new EzzarootChanneler());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("The reduction does not apply to noncreature spells")
    void doesNotReduceNoncreatureSpellCost() {
        harness.addToBattlefield(player1, new EzzarootChanneler());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The tap ability makes its controller gain 2 life")
    void gainsTwoLifeWhenTapped() {
        Permanent channeler = addCreatureReady(player1, new EzzarootChanneler());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(channeler.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Life gained by the tap ability reduces subsequent creature costs")
    void tapAbilityFeedsCostReduction() {
        addCreatureReady(player1, new EzzarootChanneler());
        Card creature = new EzzarootChanneler();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Life gain is counted only after the tap ability resolves")
    void unresolvedAbilityDoesNotReduceCost() {
        addCreatureReady(player1, new EzzarootChanneler());
        harness.setHand(player1, List.of(new EzzarootChanneler()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.getLifeGainedThisTurn(player1.getId())).isZero();
        harness.passBothPriorities();
        assertThat(gd.getLifeGainedThisTurn(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Cost reduction never pays the colored mana requirement")
    void reductionDoesNotRemoveColoredMana() {
        harness.addToBattlefield(player1, new EzzarootChanneler());
        gd.lifeGainedThisTurn.put(player1.getId(), 20);
        harness.setHand(player1, List.of(new EzzarootChanneler()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Channeler does not reduce your creature costs")
    void opposingChannelerDoesNotReduceCost() {
        harness.addToBattlefield(player2, new EzzarootChanneler());
        gd.lifeGainedThisTurn.put(player1.getId(), 5);
        harness.setHand(player1, List.of(new EzzarootChanneler()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the controller's life gain contributes to the reduction")
    void opponentsLifeGainDoesNotReduceCost() {
        harness.addToBattlefield(player1, new EzzarootChanneler());
        gd.lifeGainedThisTurn.put(player2.getId(), 5);
        harness.setHand(player1, List.of(new EzzarootChanneler()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple life gain events accumulate even when life is later lost")
    void countsTotalLifeGainRatherThanNetLifeChange() {
        addCreatureReady(player1, new EzzarootChanneler());
        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 3, "test life loss");
        addCreatureReady(player1, new EzzarootChanneler());
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(1);
        Card creature = new EzzarootChanneler();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        harness.assertLife(player1, lifeBefore + 1);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("A Channeler in hand cannot reduce its own cost")
    void channelerMustBeOnBattlefield() {
        gd.lifeGainedThisTurn.put(player1.getId(), 5);
        harness.setHand(player1, List.of(new EzzarootChanneler()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the tap ability")
    void summoningSickChannelerCannotTap() {
        Permanent channeler = harness.addToBattlefieldAndReturn(player1, new EzzarootChanneler());
        channeler.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(channeler.isTapped()).isFalse();
    }
}
