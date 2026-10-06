package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DemonlordOfAshmouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuildswornProwler;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.n.NefariousImp;
import com.github.laxika.magicalvibes.cards.v.VexingDevil;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaphaelFiendishSavior.class, VexingDevil.class, GrizzlyBears.class,
        DemonlordOfAshmouth.class, GuildswornProwler.class, Millstone.class, NefariousImp.class})
class RaphaelFiendishSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other matching creatures you control and gives them lifelink")
    void boostsMatchingCreaturesYouControl() {
        Permanent ownDevil = harness.addToBattlefieldAndReturn(player1, new VexingDevil());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentDevil = harness.addToBattlefieldAndReturn(player2, new VexingDevil());
        int devilPower = gqs.getEffectivePower(gd, ownDevil);
        int devilToughness = gqs.getEffectiveToughness(gd, ownDevil);
        int bearPower = gqs.getEffectivePower(gd, ownBear);
        int bearToughness = gqs.getEffectiveToughness(gd, ownBear);
        int opponentDevilPower = gqs.getEffectivePower(gd, opponentDevil);
        int opponentDevilToughness = gqs.getEffectiveToughness(gd, opponentDevil);

        harness.addToBattlefield(player1, new RaphaelFiendishSavior());

        assertThat(gqs.getEffectivePower(gd, ownDevil)).isEqualTo(devilPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownDevil)).isEqualTo(devilToughness + 1);
        assertThat(gqs.hasKeyword(gd, ownDevil, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(bearPower);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(bearToughness);
        assertThat(gqs.getEffectivePower(gd, opponentDevil)).isEqualTo(opponentDevilPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentDevil)).isEqualTo(opponentDevilToughness);
        assertThat(gqs.hasKeyword(gd, opponentDevil, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Creates a Devil at each end step after a creature card enters your graveyard")
    void createsDevilAfterCreatureCardEntersOwnGraveyard() {
        harness.addToBattlefield(player1, new RaphaelFiendishSavior());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));

        advanceToEndStep(player2);

        assertThat(findPermanents(player1, "Devil")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Devil when only an opponent's creature enters their graveyard")
    void doesNotCreateDevilForOpponentsGraveyard() {
        harness.addToBattlefield(player1, new RaphaelFiendishSavior());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Devil")).isEmpty();
    }

    @Test
    @DisplayName("A Devil token deals 1 damage to a chosen target when it dies")
    void devilTokenDealsDamageWhenItDies() {
        harness.addToBattlefield(player1, new RaphaelFiendishSavior());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        advanceToEndStep(player1);

        Permanent devil = findPermanents(player1, "Devil").getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, devil));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void doesNotBoostItselfOrGiveItselfLifelink() {
        Permanent raphael = harness.addToBattlefieldAndReturn(player1, new RaphaelFiendishSavior());

        assertThat(gqs.getEffectivePower(gd, raphael)).isEqualTo(raphael.getCard().getPower());
        assertThat(gqs.getEffectiveToughness(gd, raphael)).isEqualTo(raphael.getCard().getToughness());
        assertThat(gqs.hasKeyword(gd, raphael, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void boostsDemonsImpsAndTieflings() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DemonlordOfAshmouth());
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new NefariousImp());
        Permanent tiefling = harness.addToBattlefieldAndReturn(player1, new GuildswornProwler());
        harness.addToBattlefield(player1, new RaphaelFiendishSavior());

        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(5);
        for (Permanent creature : List.of(imp, tiefling)) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        }
        for (Permanent creature : List.of(demon, imp, tiefling)) {
            assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
        }
    }

    @Test
    void doesNotTriggerWithoutACreatureCardEnteringTheGraveyard() {
        harness.addToBattlefield(player1, new RaphaelFiendishSavior());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Devil")).isEmpty();
    }

    @Test
    void createsOnlyOneDevilForMultipleCreatureCards() {
        harness.addToBattlefield(player1, new RaphaelFiendishSavior());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
        });

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Devil")).hasSize(1);
    }

    @Test
    void createsDevilForMilledCreatureEvenIfRaphaelEntersAfterTheMill() {
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new RaphaelFiendishSavior());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Devil")).hasSize(1);
    }

    @Test
    void doesNotCreateDevilForMilledNoncreatureCards() {
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player1, new RaphaelFiendishSavior());
        harness.setLibrary(player1, List.of(new Millstone(), new Millstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Devil")).isEmpty();
    }

    @Test
    void creatureCardEnteringAfterEndStepBeginsDoesNotTriggerRetroactively() {
        harness.addToBattlefield(player1, new RaphaelFiendishSavior());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(findPermanents(player1, "Devil")).isEmpty();
    }

    @Test
    void devilDeathDamageRetainsLifelinkAfterRaphaelLeaves() {
        Permanent raphael = harness.addToBattlefieldAndReturn(player1, new RaphaelFiendishSavior());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        advanceToEndStep(player1);
        Permanent devil = findPermanents(player1, "Devil").getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, devil));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, raphael));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    void tokenDeathOnTheFollowingTurnDoesNotSatisfyTheCreatureCardCondition() {
        harness.addToBattlefield(player1, new RaphaelFiendishSavior());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        advanceToEndStep(player1);
        Permanent devil = findPermanents(player1, "Devil").getFirst();
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.passUntilWithNoAttackers(player2, TurnStep.POSTCOMBAT_MAIN);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, devil));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        advanceToEndStep(player2);

        assertThat(findPermanents(player1, "Devil")).isEmpty();
    }

    @Test
    void endStepTriggerStillCreatesDevilAfterRaphaelLeaves() {
        Permanent raphael = harness.addToBattlefieldAndReturn(player1, new RaphaelFiendishSavior());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, raphael));
        harness.passBothPriorities();

        Permanent devil = findPermanents(player1, "Devil").getFirst();
        assertThat(gqs.getEffectivePower(gd, devil)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, devil)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, devil, Keyword.LIFELINK)).isFalse();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
