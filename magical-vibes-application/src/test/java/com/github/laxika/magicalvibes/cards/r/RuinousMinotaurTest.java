package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuinousMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("When it deals combat damage to an opponent, its controller sacrifices a land")
    void combatDamageToOpponentSacrificesLand() {
        Permanent minotaur = addCreatureReady(player1, new RuinousMinotaur());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        addCreatureReady(player1, new GrizzlyBears());
        minotaur.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(forest.getId(), mountain.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage to a creature does not trigger the land sacrifice")
    void damageToCreatureDoesNotTrigger() {
        Permanent minotaur = addCreatureReady(player1, new RuinousMinotaur());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        minotaur.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
