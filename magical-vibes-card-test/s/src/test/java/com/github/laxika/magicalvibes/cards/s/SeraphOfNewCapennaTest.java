package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FlywheelRacer;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({SeraphOfNewCapenna.class, SeraphOfNewPhyrexia.class, GrizzlyBears.class, Ornithopter.class,
        FlywheelRacer.class})
class SeraphOfNewCapennaTest extends BaseCardTest {

    @Test
    void activatesWithPhyrexianManaAndTransformsAtSorcerySpeed() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfNewCapenna());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(seraph.isTransformed()).isTrue();
    }

    @Test
    void transformedSeraphMaySacrificeAnotherCreatureOrArtifactForPlusTwoPlusOne() {
        Permanent seraph = addTransformedSeraph();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), ornithopter.getId());

        harness.handlePermanentChosen(player1, ornithopter.getId());

        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ornithopter.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    void transformedSeraphCannotSacrificeItselfAndDecliningDoesNotBoost() {
        Permanent seraph = addTransformedSeraph();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(seraph, bears);
    }

    @Test
    void transformationPaysTwoLifeWhenBlackManaIsUnavailable() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfNewCapenna());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertLife(player1, 18);
        assertThat(seraph.isTransformed()).isFalse();
        harness.passBothPriorities();
        assertThat(seraph.isTransformed()).isTrue();
    }

    @Test
    void blackManaPaysForTransformationWithoutLosingLife() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfNewCapenna());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(seraph.isTransformed()).isTrue();
        harness.assertLife(player1, 20);
    }

    @Test
    void payingLifeStillRequiresFourMana() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfNewCapenna());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(seraph.isTransformed()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void transformationCannotBeActivatedDuringCombat() {
        addCreatureReady(player1, new SeraphOfNewCapenna());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transformationCannotBeActivatedWithAnAbilityOnTheStack() {
        addCreatureReady(player1, new SeraphOfNewCapenna());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }

    @Test
    void maySacrificeANoncreatureArtifactAndBoostExpiresAtEndOfTurn() {
        Permanent seraph = addTransformedSeraph();
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new FlywheelRacer());
        Permanent opposingCreature = addCreatureReady(player2, new SeraphOfNewCapenna());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(vehicle.getId());
        harness.handlePermanentChosen(player1, vehicle.getId());

        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vehicle.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(3);
    }

    @Test
    void maySacrificeAnotherCreatureOfTheSameName() {
        Permanent seraph = addTransformedSeraph();
        Permanent otherSeraph = addCreatureReady(player1, new SeraphOfNewCapenna());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherSeraph.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherSeraph.getCard());
        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(4);
    }

    @Test
    void frontFaceAttackDoesNotOfferSacrificeOrBoost() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfNewCapenna());
        Permanent otherSeraph = addCreatureReady(player1, new SeraphOfNewCapenna());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(seraph, otherSeraph);
        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(2);
    }

    @Test
    void acceptingWithNoOtherPermanentDoesNotSacrificeSelfOrBoost() {
        Permanent seraph = addTransformedSeraph();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(seraph);
        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addTransformedSeraph() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfNewCapenna());
        seraph.setCard(seraph.getCard().getBackFaceCard());
        seraph.setTransformed(true);
        return seraph;
    }

}
