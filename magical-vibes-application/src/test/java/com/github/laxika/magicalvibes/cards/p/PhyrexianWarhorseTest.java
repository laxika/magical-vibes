package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianWarhorse.class, GrizzlyBears.class})
class PhyrexianWarhorseTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Soldier token when cast with kicker")
    void kickedCastCreatesSoldier() {
        harness.setHand(player1, List.of(new PhyrexianWarhorse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(soldiers(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Soldier token when cast without kicker")
    void nonKickedCastDoesNotCreateSoldier() {
        harness.setHand(player1, List.of(new PhyrexianWarhorse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(soldiers(player1)).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing another creature gives Phyrexian Warhorse +2/+1 until end of turn")
    void sacrificeAnotherCreatureBoostsWarhorse() {
        Permanent warhorse = addCreatureReady(player1, new PhyrexianWarhorse());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(warhorse.getPowerModifier()).isEqualTo(2);
        assertThat(warhorse.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warhorse.getPowerModifier()).isEqualTo(0);
        assertThat(warhorse.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot sacrifice Phyrexian Warhorse itself")
    void cannotSacrificeItself() {
        addCreatureReady(player1, new PhyrexianWarhorse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The kicked Soldier can pay the cost immediately while Warhorse has summoning sickness")
    void sacrificeKickedSoldierPaysCostBeforeBoostResolves() {
        harness.setHand(player1, List.of(new PhyrexianWarhorse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        Permanent warhorse = findPermanent(player1, "Phyrexian Warhorse");
        Permanent soldier = soldiers(player1).getFirst();
        assertThat(soldier.getCard().getPower()).isEqualTo(1);
        assertThat(soldier.getCard().getToughness()).isEqualTo(1);
        assertThat(soldiers(player2)).isEmpty();

        harness.activateAbility(player1, 0, null, null);

        assertThat(soldiers(player1)).isEmpty();
        assertThat(warhorse.getPowerModifier()).isZero();
        assertThat(warhorse.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(warhorse.getPowerModifier()).isEqualTo(2);
        assertThat(warhorse.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations add their boosts and can be used while tapped")
    void repeatedActivationsAccumulate() {
        Permanent warhorse = addCreatureReady(player1, new PhyrexianWarhorse());
        Permanent firstSacrifice = addCreatureReady(player1, new PhyrexianWarhorse());
        addCreatureReady(player1, new PhyrexianWarhorse());
        warhorse.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstSacrifice.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Warhorse")).containsExactly(warhorse);
        assertThat(warhorse.getPowerModifier()).isEqualTo(4);
        assertThat(warhorse.getToughnessModifier()).isEqualTo(2);
        assertThat(warhorse.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warhorse.getPowerModifier()).isZero();
        assertThat(warhorse.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addCreatureReady(player1, new PhyrexianWarhorse());
        Permanent opponentCreature = addCreatureReady(player2, new PhyrexianWarhorse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a creature does not replace the required mana payment")
    void cannotActivateWithoutMana() {
        Permanent warhorse = addCreatureReady(player1, new PhyrexianWarhorse());
        Permanent sacrifice = addCreatureReady(player1, new PhyrexianWarhorse());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(warhorse, sacrifice);
        assertThat(gd.stack).isEmpty();
    }

    private List<Permanent> soldiers(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .toList();
    }
}
