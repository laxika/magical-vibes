package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({MavindaStudentsAdvocate.class, GiantGrowth.class, GrizzlyBears.class, Shock.class})
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

    private void grantPermission(Card card) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(card.getId()));
        harness.passBothPriorities();
    }
}
