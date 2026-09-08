package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlintSleeveSiphonerTest extends BaseCardTest {

    @Test
    void getsEnergyWhenItEntersAndAttacks() {
        harness.setHand(player1, List.of(new GlintSleeveSiphoner()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent siphoner = findPermanent(player1, "Glint-Sleeve Siphoner");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);

        siphoner.setSummoningSick(false);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void paysTwoEnergyToDrawAndLoseLifeDuringUpkeep() {
        addCreatureReady(player1, new GlintSleeveSiphoner());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        gd.playerEnergyCounters.put(player1.getId(), 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void decliningTheUpkeepPaymentDoesNothing() {
        addCreatureReady(player1, new GlintSleeveSiphoner());
        harness.setLibrary(player1, List.of(new Forest()));
        gd.playerEnergyCounters.put(player1.getId(), 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void cannotPayPartiallyForTheUpkeepAbility() {
        addCreatureReady(player1, new GlintSleeveSiphoner());
        harness.setLibrary(player1, List.of(new Forest()));
        gd.playerEnergyCounters.put(player1.getId(), 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
