package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TakeUpTheShield;
import com.github.laxika.magicalvibes.cards.t.ThousandYearElixir;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArgivianCavalier.class, GrizzlyBears.class, TakeUpTheShield.class, ThousandYearElixir.class})
class ArgivianCavalierTest extends BaseCardTest {

    @Test
    @DisplayName("When Argivian Cavalier enters, it creates a 1/1 white Soldier token")
    void etbCreatesSoldierToken() {
        harness.setHand(player1, List.of(new ArgivianCavalier()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().getPower()).isEqualTo(1);
        assertThat(soldier.getCard().getToughness()).isEqualTo(1);
        assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldier.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(soldier.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Enlist taps a nonattacking creature and gives Argivian Cavalier its power")
    void enlistBoostsAttackerBySupporterPower() {
        Permanent cavalier = addCreatureReady(player1, new ArgivianCavalier());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        assertThat(supporter.isTapped()).isTrue();
        assertThat(cavalier.getPowerModifier()).isZero();

        harness.passBothPriorities();
        assertThat(cavalier.getPowerModifier()).isEqualTo(2);
        assertThat(cavalier.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Enlist cannot use a creature with summoning sickness")
    void enlistExcludesSummoningSickCreature() {
        Permanent cavalier = addCreatureReady(player1, new ArgivianCavalier());
        harness.addToBattlefield(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(cavalier.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Enlist may be declined without tapping the supporter")
    void enlistMayBeDeclined() {
        Permanent cavalier = addCreatureReady(player1, new ArgivianCavalier());
        Permanent supporter = addCreatureReady(player1, new ArgivianCavalier());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(supporter.isTapped()).isFalse();
        assertThat(cavalier.getPowerModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enlist excludes tapped creatures, other attackers, and opposing creatures")
    void enlistExcludesIneligibleSupporters() {
        Permanent cavalier = addCreatureReady(player1, new ArgivianCavalier());
        addCreatureReady(player1, new ArgivianCavalier());
        Permanent tapped = addCreatureReady(player1, new ArgivianCavalier());
        tapped.tap();
        addCreatureReady(player2, new ArgivianCavalier());

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(cavalier.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Enlist uses the supporter's power when the trigger resolves")
    void enlistUsesPowerAtResolution() {
        Permanent cavalier = addCreatureReady(player1, new ArgivianCavalier());
        Permanent supporter = addCreatureReady(player1, new ArgivianCavalier());
        harness.setHand(player1, List.of(new TakeUpTheShield()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.castAndResolveInstant(player1, 0, supporter.getId());
        assertThat(cavalier.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(cavalier.getPowerModifier()).isEqualTo(3);
        assertThat(cavalier.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Enlist's power bonus expires at end of turn")
    void enlistBonusExpiresAtEndOfTurn() {
        Permanent cavalier = addCreatureReady(player1, new ArgivianCavalier());
        Permanent supporter = addCreatureReady(player1, new ArgivianCavalier());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();
        assertThat(cavalier.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cavalier.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Permission to activate abilities as though having haste does not allow enlist")
    void activationPermissionDoesNotAllowEnlistingSummoningSickCreature() {
        Permanent cavalier = addCreatureReady(player1, new ArgivianCavalier());
        harness.addToBattlefield(player1, new ArgivianCavalier());
        harness.addToBattlefield(player1, new ThousandYearElixir());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(cavalier.getPowerModifier()).isZero();
    }
}
