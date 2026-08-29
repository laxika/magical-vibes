package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilumgarTheDriftingDeath.class, GrizzlyBears.class, ShivanDragon.class})
class SilumgarTheDriftingDeathTest extends BaseCardTest {

    @Test
    void attackingDragonShrinksDefendingCreaturesOnly() {
        Permanent silumgar = addCreatureReady(player1, new SilumgarTheDriftingDeath());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(silumgar.getPowerModifier()).isZero();
        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(defendingCreature.getPowerModifier()).isEqualTo(-1);
        assertThat(defendingCreature.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    void attackingNonDragonDoesNotTrigger() {
        addCreatureReady(player1, new SilumgarTheDriftingDeath());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(defendingCreature.getPowerModifier()).isZero();
        assertThat(defendingCreature.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachAttackingDragonCreatesItsOwnDebuff() {
        addCreatureReady(player1, new SilumgarTheDriftingDeath());
        addCreatureReady(player1, new ShivanDragon());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(defendingCreature.getPowerModifier()).isEqualTo(-2);
        assertThat(defendingCreature.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    void debuffWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SilumgarTheDriftingDeath());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(defendingCreature.getToughnessModifier()).isEqualTo(-1);

        new TurnCleanupService(null, null).resetEndOfTurnModifiers(gd);

        assertThat(defendingCreature.getPowerModifier()).isZero();
        assertThat(defendingCreature.getToughnessModifier()).isZero();
    }
}
