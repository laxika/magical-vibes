package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PatientNaturalist;
import com.github.laxika.magicalvibes.cards.s.ShootTheSheriff;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AloeAlchemist.class, Forest.class, PatientNaturalist.class, ShootTheSheriff.class})
class AloeAlchemistTest extends BaseCardTest {

    @Test
    void becomingPlottedBoostsTargetCreatureAndGrantsTrampleUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatientNaturalist());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        AloeAlchemist aloe = new AloeAlchemist();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(aloe));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(target.getId()).doesNotContain(forest.getId());
        assertThat(choice.validPlayerIds()).isEmpty();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void becomingPlottedDoesNotCreateAnAbilityWithNoCreatureTarget() {
        AloeAlchemist aloe = new AloeAlchemist();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(aloe));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void normalCastingDoesNotTriggerThePlotAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PatientNaturalist());

        harness.castFromHand(player1, new AloeAlchemist(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aloe Alchemist");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void plottedCardCanBeCastForFreeOnALaterTurnButNotTheSameTurn() {
        AloeAlchemist aloe = new AloeAlchemist();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(aloe));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());

        harness.assertNotInHand(player1, "Aloe Alchemist");
        harness.assertNotOnBattlefield(player1, "Aloe Alchemist");
        assertThatThrownBy(() -> harness.castFromExile(player1, aloe.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, aloe.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aloe Alchemist");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    void plotAbilityDoesNotAffectAnotherCreatureWhenItsTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PatientNaturalist());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new PatientNaturalist());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AloeAlchemist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player1, List.of(new ShootTheSheriff()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Patient Naturalist");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void plottingIsUnavailableOutsideAMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new AloeAlchemist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Aloe Alchemist");
        assertThat(gd.stack).isEmpty();
    }
}
