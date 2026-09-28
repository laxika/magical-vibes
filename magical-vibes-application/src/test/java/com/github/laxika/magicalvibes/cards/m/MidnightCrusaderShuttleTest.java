package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MidnightCrusaderShuttle.class, GrizzlyBears.class})
class MidnightCrusaderShuttleTest extends BaseCardTest {

    @Test
    void defenderSacrificesCreature() {
        Permanent shuttle = addReadyShuttle();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        crewShuttle();
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(defendingCreature);
    }

    @Test
    void defenderDeclinesAndAttackerTakesCreatureTappedAndAttacking() {
        Permanent shuttle = addReadyShuttle();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherDefendingCreature = addCreatureReady(player2, new GrizzlyBears());

        crewShuttle();
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handlePermanentChosen(player1, defendingCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(defendingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherDefendingCreature);
        assertThat(defendingCreature.isTapped()).isTrue();
        assertThat(defendingCreature.isAttacking()).isTrue();
        assertThat(defendingCreature.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(shuttle.isAttacking()).isTrue();
    }

    private Permanent addReadyShuttle() {
        Permanent shuttle = new Permanent(new MidnightCrusaderShuttle());
        shuttle.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(shuttle);
        return shuttle;
    }

    private void crewShuttle() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
