package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HexbaneTortoise.class, GrizzlyBears.class, Shock.class})
class HexbaneTortoiseTest extends BaseCardTest {

    @Test
    @DisplayName("Enlist taps a nonattacking creature and boosts Hexbane Tortoise by its power")
    void enlistBoostsAttackerBySupporterPower() {
        Permanent tortoise = addCreatureReady(player1, new HexbaneTortoise());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isTrue();
        assertThat(tortoise.getPowerModifier()).isEqualTo(2);
        assertThat(tortoise.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when its controller cannot pay {2}")
    void wardCountersSpellWhenOpponentCannotPay() {
        Permanent tortoise = addCreatureReady(player1, new HexbaneTortoise());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, tortoise.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enlistCanBeDeclined() {
        Permanent tortoise = addCreatureReady(player1, new HexbaneTortoise());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(supporter.isTapped()).isFalse();
        assertThat(tortoise.getPowerModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enlistExcludesTappedSummoningSickAttackingAndOpposingCreatures() {
        addCreatureReady(player1, new HexbaneTortoise());
        Permanent eligible = addCreatureReady(player1, new GrizzlyBears());
        Permanent tapped = addCreatureReady(player1, new GrizzlyBears());
        tapped.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 4));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(eligible.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());
    }

    @Test
    void enlistUsesSupporterPowerWhenTriggerResolves() {
        Permanent tortoise = addCreatureReady(player1, new HexbaneTortoise());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        assertThat(tortoise.getPowerModifier()).isZero();
        supporter.setPowerModifier(3);
        harness.passBothPriorities();

        assertThat(tortoise.getPowerModifier()).isEqualTo(5);
        assertThat(tortoise.getToughnessModifier()).isZero();
    }

    @Test
    void enlistWithNegativePowerGivesNoBonus() {
        Permanent tortoise = addCreatureReady(player1, new HexbaneTortoise());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());
        supporter.setPowerModifier(-3);

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isTrue();
        assertThat(tortoise.getPowerModifier()).isZero();
    }

    @Test
    void enlistUsesLastKnownPowerWhenSupporterDiesInResponse() {
        Permanent tortoise = addCreatureReady(player1, new HexbaneTortoise());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.castInstant(player1, 0, supporter.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        resolveAllTriggers();

        assertThat(tortoise.getPowerModifier()).isEqualTo(2);
        assertThat(tortoise.getToughnessModifier()).isZero();
    }

    @Test
    void wardAllowsSpellWhenOpponentPays() {
        Permanent tortoise = addCreatureReady(player1, new HexbaneTortoise());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, tortoise.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Hexbane Tortoise");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardDoesNotTriggerForControllersSpell() {
        Permanent tortoise = addCreatureReady(player1, new HexbaneTortoise());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, tortoise.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hexbane Tortoise");
    }
}
