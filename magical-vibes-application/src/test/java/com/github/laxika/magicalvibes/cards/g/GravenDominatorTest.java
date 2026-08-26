package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

@CardUsed({GravenDominator.class, GrizzlyBears.class, DoomBlade.class, LightningBolt.class})
class GravenDominatorTest extends BaseCardTest {

    @Test
    void enteringSetsEachOtherCreatureToOneOneUntilEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent ownBear = findPermanents(player1, "Grizzly Bears").getFirst();
        Permanent opposingBear = findPermanents(player2, "Grizzly Bears").getFirst();

        castGravenDominator();

        Permanent gravenDominator = findPermanents(player1, "Graven Dominator").getFirst();
        assertThat(gqs.getEffectivePower(gd, gravenDominator)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gravenDominator)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
    }

    @Test
    void hauntedCreatureDeathSetsOtherCreaturesToOneOne() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent hauntedBear = findPermanents(player2, "Grizzly Bears").getFirst();
        Permanent survivingBear = findPermanents(player2, "Grizzly Bears").get(1);

        castGravenDominator();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        UUID gravenDominatorId = harness.getPermanentId(player1, "Graven Dominator");
        destroyWithDoomBlade(gravenDominatorId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, hauntedBear.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Graven Dominator");

        destroyWithLightningBolt(hauntedBear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, survivingBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, survivingBear)).isEqualTo(1);
    }

    private void castGravenDominator() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GravenDominator()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyWithDoomBlade(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
    }

    private void destroyWithLightningBolt(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
    }
}
