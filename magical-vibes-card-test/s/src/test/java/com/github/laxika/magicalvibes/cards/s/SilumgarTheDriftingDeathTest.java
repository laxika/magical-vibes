package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilumgarTheDriftingDeath.class, GrizzlyBears.class, ShivanDragon.class, Unsummon.class})
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

        GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd);

        assertThat(defendingCreature.getPowerModifier()).isZero();
        assertThat(defendingCreature.getToughnessModifier()).isZero();
    }

    @Test
    void anotherDragonTriggersWhileSilumgarDoesNotAttack() {
        Permanent silumgar = addCreatureReady(player1, new SilumgarTheDriftingDeath());
        addCreatureReady(player1, new ShivanDragon());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(silumgar.isAttacking()).isFalse();
        assertThat(defender.getPowerModifier()).isEqualTo(-1);
        assertThat(defender.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    void opposingDragonDoesNotTriggerSilumgar() {
        addCreatureReady(player1, new SilumgarTheDriftingDeath());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new ShivanDragon());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
    }

    @Test
    void affectedCreaturesAreDeterminedAtResolution() {
        addCreatureReady(player1, new SilumgarTheDriftingDeath());
        Permanent original = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        Permanent beforeResolution = addCreatureReady(player2, new GrizzlyBears());
        resolveAllTriggers();
        Permanent afterResolution = addCreatureReady(player2, new GrizzlyBears());

        assertThat(original.getToughnessModifier()).isEqualTo(-1);
        assertThat(beforeResolution.getPowerModifier()).isEqualTo(-1);
        assertThat(beforeResolution.getToughnessModifier()).isEqualTo(-1);
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
    }

    @Test
    void removingOnlyAttackerDoesNotPreventDebuff() {
        addCreatureReady(player1, new SilumgarTheDriftingDeath());
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player2, 0, dragon.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Shivan Dragon");
        harness.assertNotOnBattlefield(player1, "Shivan Dragon");
        assertThat(defender.getPowerModifier()).isEqualTo(-1);
        assertThat(defender.getToughnessModifier()).isEqualTo(-1);
    }
}
