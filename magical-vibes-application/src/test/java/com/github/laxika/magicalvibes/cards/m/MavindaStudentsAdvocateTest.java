package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Tidings;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MavindaStudentsAdvocate.class, GiantGrowth.class, GrizzlyBears.class, Shock.class, Tidings.class})
class MavindaStudentsAdvocateTest extends BaseCardTest {

    @Test
    void castsSpellTargetingCreatureControlledByCasterWithoutAdditionalCost() {
        harness.addToBattlefield(player1, new MavindaStudentsAdvocate());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card giantGrowth = new GiantGrowth();
        harness.setGraveyard(player1, List.of(giantGrowth));

        grantPermission(giantGrowth);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromGraveyardTargeting(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(giantGrowth.getId()));
    }

    @Test
    void chargesAdditionalGenericCostForSpellNotTargetingControlledCreature() {
        harness.addToBattlefield(player1, new MavindaStudentsAdvocate());
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        grantPermission(shock);
        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    void canActivateOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new MavindaStudentsAdvocate());
        Card shock = new Shock();
        Card giantGrowth = new GiantGrowth();
        harness.setGraveyard(player1, List.of(shock, giantGrowth));

        grantPermission(shock);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(giantGrowth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chargesAdditionalCostWhenTargetingOpponentsCreature() {
        harness.addToBattlefield(player1, new MavindaStudentsAdvocate());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card giantGrowth = new GiantGrowth();
        harness.setGraveyard(player1, List.of(giantGrowth));
        grantPermission(giantGrowth);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castFromGraveyardTargeting(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(giantGrowth.getId()));
    }

    @Test
    void rejectsCreatureCardAndOpponentsGraveyardAsAbilityTargets() {
        harness.addToBattlefield(player1, new MavindaStudentsAdvocate());
        Card creature = new GrizzlyBears();
        Card opponentsSpell = new GiantGrowth();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(opponentsSpell));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(opponentsSpell.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void permissionSurvivesMavindaLeavingBattlefield() {
        harness.addToBattlefield(player1, new MavindaStudentsAdvocate());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card giantGrowth = new GiantGrowth();
        harness.setGraveyard(player1, List.of(giantGrowth));
        grantPermission(giantGrowth);
        gd.playerBattlefields.get(player1.getId()).remove(0);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromGraveyardTargeting(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(giantGrowth.getId()));
    }

    @Test
    void exilesSpellWhenItsOnlyTargetBecomesIllegal() {
        harness.addToBattlefield(player1, new MavindaStudentsAdvocate());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card giantGrowth = new GiantGrowth();
        harness.setGraveyard(player1, List.of(giantGrowth));
        grantPermission(giantGrowth);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromGraveyardTargeting(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(giantGrowth.getId()));
        harness.assertNotInGraveyard(player1, "Giant Growth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void untargetedSorceryPaysEightMoreAndRetainsSorceryTiming() {
        harness.addToBattlefield(player1, new MavindaStudentsAdvocate());
        Card tidings = new Tidings();
        harness.setGraveyard(player1, List.of(tidings));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        grantPermission(tidings);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(tidings.getId()));
    }

    @Test
    void canActivateAndCastInstantDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new MavindaStudentsAdvocate());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card giantGrowth = new GiantGrowth();
        harness.setGraveyard(player1, List.of(giantGrowth));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(giantGrowth.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromGraveyardTargeting(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(giantGrowth.getId()));
    }

    @Test
    void permissionExpiresAndActivationLimitResetsOnNextTurn() {
        harness.addToBattlefield(player1, new MavindaStudentsAdvocate());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card giantGrowth = new GiantGrowth();
        harness.setGraveyard(player1, List.of(giantGrowth));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        grantPermission(giantGrowth);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(giantGrowth.getId()));
        harness.passBothPriorities();
        harness.castFromGraveyardTargeting(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(giantGrowth.getId()));
    }

    private void grantPermission(Card card) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(card.getId()));
        harness.passBothPriorities();
    }
}
