package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SalvationColossus.class, GrizzlyBears.class})
class SalvationColossusTest extends BaseCardTest {

    @Test
    void attackTriggerBoostsOtherOwnCreaturesAndGrantsIndestructible() {
        Permanent colossus = addCreatureReady(player1, new SalvationColossus());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, colossus, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    void attackBonusWearsOffAtEndOfTurn() {
        Permanent colossus = addCreatureReady(player1, new SalvationColossus());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void energyUnearthReturnsThisCardWithHasteAndExilesItAtNextEndStep() {
        harness.setGraveyard(player1, List.of(new SalvationColossus()));
        gd.playerEnergyCounters.put(player1.getId(), 8);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Salvation Colossus");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Salvation Colossus");
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card ->
                card.getName().equals("Salvation Colossus"));
    }

    @Test
    void cannotUnearthWithoutEightEnergy() {
        harness.setGraveyard(player1, List.of(new SalvationColossus()));
        gd.playerEnergyCounters.put(player1.getId(), 7);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleAttackersGiveOnlyOneBonusIncludingToNonattackers() {
        addCreatureReady(player1, new SalvationColossus());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        for (Permanent creature : List.of(first, second, nonattacker)) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
        }
    }

    @Test
    void attackingWithColossusAloneBoostsOtherCreaturesButNotItself() {
        Permanent colossus = addCreatureReady(player1, new SalvationColossus());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, colossus, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, other, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotReceiveBonus() {
        addCreatureReady(player1, new SalvationColossus());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(1));
        resolveAllTriggers();

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void attackTriggerStillResolvesAfterColossusLeavesBattlefield() {
        Permanent colossus = addCreatureReady(player1, new SalvationColossus());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(1)));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, colossus));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Salvation Colossus");
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void opponentsAttackDoesNotTriggerBonus() {
        addCreatureReady(player1, new SalvationColossus());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void unearthedColossusIsExiledInsteadOfBeingSacrificedToGraveyard() {
        harness.setGraveyard(player1, List.of(new SalvationColossus()));
        gd.playerEnergyCounters.put(player1.getId(), 8);
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        Permanent returned = findPermanent(player1, "Salvation Colossus");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, returned));

        harness.assertNotOnBattlefield(player1, "Salvation Colossus");
        harness.assertNotInGraveyard(player1, "Salvation Colossus");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(returned.getCard());
    }

    @Test
    void unearthReturnsOnlyTheActivatedCopyAndPaysEnergyImmediately() {
        SalvationColossus first = new SalvationColossus();
        SalvationColossus second = new SalvationColossus();
        harness.setGraveyard(player1, List.of(first, second));
        gd.playerEnergyCounters.put(player1.getId(), 10);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Salvation Colossus");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Salvation Colossus").getCard()).isSameAs(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
    }

    @Test
    void cannotUnearthDuringCombat() {
        harness.setGraveyard(player1, List.of(new SalvationColossus()));
        gd.playerEnergyCounters.put(player1.getId(), 8);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(8);
        harness.assertInGraveyard(player1, "Salvation Colossus");
    }
}
