package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrzhovPontiff.class, OrzhovGuildmage.class, Mortify.class})
class OrzhovPontiffTest extends BaseCardTest {

    private static final String OWN_CREATURES = "Creatures you control get +1/+1 until end of turn";
    private static final String OPPONENT_CREATURES = "Creatures you don't control get -1/-1 until end of turn";

    @Test
    void entersAndBoostsCreaturesYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new OrzhovGuildmage());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new OrzhovGuildmage());

        castPontiff();
        harness.handleListChoice(player1, OWN_CREATURES);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    void entersAndShrinksCreaturesYouDoNotControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new OrzhovGuildmage());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new OrzhovGuildmage());

        castPontiff();
        harness.handleListChoice(player1, OPPONENT_CREATURES);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
    }

    @Test
    void hauntedCreatureDeathTriggersTheSameModalAbility() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new OrzhovGuildmage());
        Permanent hauntedCreature = harness.addToBattlefieldAndReturn(player2, new OrzhovGuildmage());
        Permanent unrelatedCreature = harness.addToBattlefieldAndReturn(player2, new OrzhovGuildmage());

        castPontiff();
        harness.handleListChoice(player1, OPPONENT_CREATURES);
        harness.passBothPriorities();

        UUID pontiffId = harness.getPermanentId(player1, "Orzhov Pontiff");
        destroyWithMortify(pontiffId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, hauntedCreature.getId());
        harness.passBothPriorities();

        destroyWithMortify(unrelatedCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        destroyWithMortify(hauntedCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, OWN_CREATURES);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Orzhov Pontiff");
    }

    @Test
    void hauntedCreatureDeathCanShrinkCreaturesYouDoNotControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new OrzhovGuildmage());
        Permanent hauntedCreature = harness.addToBattlefieldAndReturn(player2, new OrzhovGuildmage());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new OrzhovGuildmage());

        castPontiff();
        harness.handleListChoice(player1, OWN_CREATURES);
        harness.passBothPriorities();

        UUID pontiffId = harness.getPermanentId(player1, "Orzhov Pontiff");
        destroyWithMortify(pontiffId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, hauntedCreature.getId());
        harness.passBothPriorities();

        destroyWithMortify(hauntedCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, OPPONENT_CREATURES);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
    }

    @Test
    void boostIncludesPontiffButNotCreaturesEnteringAfterResolutionAndExpires() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new OrzhovGuildmage());

        castPontiff();
        harness.handleListChoice(player1, OWN_CREATURES);
        harness.passBothPriorities();

        Permanent pontiff = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Orzhov Pontiff"));
        Permanent lateCreature = harness.enterBattlefieldAndReturn(player1, new OrzhovGuildmage());
        assertThat(gqs.getEffectivePower(gd, pontiff)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pontiff)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, pontiff)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pontiff)).isEqualTo(1);
    }

    @Test
    void shrinkDoesNotAffectCreaturesEnteringAfterResolutionAndExpires() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new OrzhovGuildmage());

        castPontiff();
        harness.handleListChoice(player1, OPPONENT_CREATURES);
        harness.passBothPriorities();

        Permanent lateCreature = harness.enterBattlefieldAndReturn(player2, new OrzhovGuildmage());
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    void hauntDoesNotExilePontiffWhenItsTargetDiesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrzhovGuildmage());

        castPontiff();
        harness.handleListChoice(player1, OWN_CREATURES);
        harness.passBothPriorities();

        destroyWithMortify(harness.getPermanentId(player1, "Orzhov Pontiff"));
        harness.handlePermanentChosen(player1, target.getId());
        destroyWithMortify(target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Orzhov Pontiff");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getName)
                .doesNotContain("Orzhov Pontiff");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castPontiff() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new OrzhovPontiff(), "{1}{W}{B}");
        harness.passBothPriorities();
    }

    private void destroyWithMortify(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Mortify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
    }
}
