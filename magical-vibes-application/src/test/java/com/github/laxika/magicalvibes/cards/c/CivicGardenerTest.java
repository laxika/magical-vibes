package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CivicGardener.class, Forest.class, GrizzlyBears.class, SolRing.class})
class CivicGardenerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking presents creatures and lands as targets")
    void attackTriggerTargetsCreatureOrLand() {
        addReadyGardener();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(creature.getId(), land.getId())
                .doesNotContain(artifact.getId());
    }

    @Test
    @DisplayName("Attacking untaps the chosen creature")
    void attackTriggerUntapsCreature() {
        addReadyGardener();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attacking untaps the chosen land")
    void attackTriggerUntapsLand() {
        addReadyGardener();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
    }

    private Permanent addReadyGardener() {
        Permanent gardener = harness.addToBattlefieldAndReturn(player1, new CivicGardener());
        gardener.setSummoningSick(false);
        return gardener;
    }
}
