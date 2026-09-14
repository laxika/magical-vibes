package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonOfLoathing.class, GrizzlyBears.class, Forest.class})
class DemonOfLoathingTest extends BaseCardTest {

    @Test
    @DisplayName("The damaged player chooses a creature they control to sacrifice")
    void damagedPlayerChoosesCreature() {
        Permanent demon = addCreatureReady(player1, new DemonOfLoathing());
        demon.setAttacking(true);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherEnemyCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(enemyCreature.getId(), otherEnemyCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player2, enemyCreature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(otherEnemyCreature)
                .doesNotContain(enemyCreature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No sacrifice trigger occurs when the Demon deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        Permanent demon = addCreatureReady(player1, new DemonOfLoathing());
        demon.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.addToBattlefield(player2, new Forest());

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 7));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Forest");
    }
}
