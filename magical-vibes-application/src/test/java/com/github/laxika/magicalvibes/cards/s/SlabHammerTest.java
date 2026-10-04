package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlabHammer.class, GrizzlyBears.class, Mountain.class})
class SlabHammerTest extends BaseCardTest {

    @Test
    void returningALandBoostsTheEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hammer = addCreatureReady(player1, new SlabHammer());
        hammer.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new Mountain());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Mountain"));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    void decliningDoesNotReturnALandOrBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hammer = addCreatureReady(player1, new SlabHammer());
        hammer.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new Mountain());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    void returnChoiceOnlyOffersLandsYouControl() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hammer = addCreatureReady(player1, new SlabHammer());
        hammer.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        var ownMountainId = harness.getPermanentId(player1, "Mountain");

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ownMountainId);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hammer = addCreatureReady(player1, new SlabHammer());
        hammer.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new Mountain());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Mountain"));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }
}
