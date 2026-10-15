package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.q.QueenOfIce;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MantleOfTides.class, CounselOfTheSoratami.class, GrizzlyBears.class, Opt.class, QueenOfIce.class})
class MantleOfTidesTest extends BaseCardTest {

    @Test
    void secondDrawTargetsCreatureYouControlAndAttachesMantle() {
        Permanent mantle = harness.addToBattlefieldAndReturn(player1, new MantleOfTides());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DrawTriggerPermanentTarget.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(mantle.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void equipAttachesMantleAndGrantsItsBonus() {
        Permanent mantle = harness.addToBattlefieldAndReturn(player1, new MantleOfTides());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mantle.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void onlySecondDrawTriggersOnOpponentsTurnAndMovesEquipment() {
        Permanent mantle = harness.addToBattlefieldAndReturn(player1, new MantleOfTides());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new QueenOfIce());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new QueenOfIce());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, firstCreature.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new QueenOfIce(), new QueenOfIce(), new QueenOfIce()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        castOptAndKeepTop();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(mantle.getAttachedTo()).isEqualTo(firstCreature.getId());

        castOptAndKeepTop();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(mantle.getAttachedTo()).isEqualTo(firstCreature.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();
        assertThat(mantle.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(5);

        castOptAndKeepTop();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(mantle.getAttachedTo()).isEqualTo(secondCreature.getId());
    }

    @Test
    void opponentsSecondDrawDoesNotTriggerMantle() {
        Permanent mantle = harness.addToBattlefieldAndReturn(player1, new MantleOfTides());
        harness.addToBattlefield(player1, new QueenOfIce());
        harness.setLibrary(player2, List.of(new CounselOfTheSoratami(), new CounselOfTheSoratami()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new CounselOfTheSoratami()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(mantle.getAttachedTo()).isNull();
    }

    @Test
    void secondDrawWithNoControlledCreatureDoesNotRequireTargetChoice() {
        Permanent mantle = harness.addToBattlefieldAndReturn(player1, new MantleOfTides());
        harness.addToBattlefield(player2, new QueenOfIce());
        harness.setLibrary(player1, List.of(new QueenOfIce(), new QueenOfIce()));

        castOptAndKeepTop();
        castOptAndKeepTop();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(mantle.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotBeActivatedOnOpponentsTurnOrTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new MantleOfTides());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new QueenOfIce());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new QueenOfIce());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castOptAndKeepTop() {
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }
}
