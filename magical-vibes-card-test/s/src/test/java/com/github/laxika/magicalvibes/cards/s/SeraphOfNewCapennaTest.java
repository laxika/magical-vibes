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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeraphOfNewCapenna.class, SeraphOfNewPhyrexia.class, GrizzlyBears.class, Ornithopter.class,
        FlywheelRacer.class})
class SeraphOfNewCapennaTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {4} and Phyrexian mana transforms it at sorcery speed")
    void activatesWithPhyrexianManaAndTransformsAtSorcerySpeed() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfNewCapenna());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(seraph.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("With several other creatures or artifacts, you choose one to sacrifice for +2/+1")
    void transformedSeraphMaySacrificeAnotherCreatureOrArtifactForPlusTwoPlusOne() {
        Permanent seraph = addTransformedSeraph();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                (PendingInteraction.MultiPermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), ornithopter.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player1, List.of(ornithopter.getId()));

        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ornithopter.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Declining the sacrifice keeps every permanent and gives no boost")
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
    @DisplayName("Without black mana, transforming costs 2 life")
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
    @DisplayName("Black mana pays for transforming without losing life")
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
    @DisplayName("Paying life does not replace the {4} in the transform cost")
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
    @DisplayName("Transforming cannot be activated during combat")
    void transformationCannotBeActivatedDuringCombat() {
        addCreatureReady(player1, new SeraphOfNewCapenna());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Transforming cannot be activated while an ability is on the stack")
    void transformationCannotBeActivatedWithAnAbilityOnTheStack() {
        addCreatureReady(player1, new SeraphOfNewCapenna());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A lone noncreature artifact you control is sacrificed without a choice, and the boost expires at end of turn")
    void maySacrificeANoncreatureArtifactAndBoostExpiresAtEndOfTurn() {
        Permanent seraph = addTransformedSeraph();
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new FlywheelRacer());
        Permanent opposingCreature = addCreatureReady(player2, new SeraphOfNewCapenna());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(seraph);
        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vehicle.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);

        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(3);
    }

    @Test
    @DisplayName("A lone other creature of the same name is sacrificed without a choice")
    void maySacrificeAnotherCreatureOfTheSameName() {
        Permanent seraph = addTransformedSeraph();
        Permanent otherSeraph = addCreatureReady(player1, new SeraphOfNewCapenna());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(seraph);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherSeraph.getCard());
        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(4);
    }

    @Test
    @DisplayName("The front face attacking offers no sacrifice and gives no boost")
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
    @DisplayName("With no other creature or artifact, the sacrifice is not offered and the Seraph is not boosted")
    void withNoOtherPermanentSacrificeIsNotOfferedAndSeraphIsNotBoosted() {
        Permanent seraph = addTransformedSeraph();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
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
