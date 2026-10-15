package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeholdThePowerOfDestruction.class, GrizzlyBears.class, Island.class,
        BurnishedHart.class, CommandTower.class, SolRing.class})
class BeholdThePowerOfDestructionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all nonland permanents controlled by the target opponent")
    void destroysTargetOpponentsNonlands() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        BeholdThePowerOfDestruction scheme = new BeholdThePowerOfDestruction();

        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL),
                player2.getId(),
                (Zone) null));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
    }

    @Test
    @DisplayName("Destroys artifacts as well as creatures and preserves nonbasic lands")
    void destroysNoncreatureArtifactsButPreservesNonbasicLands() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CommandTower());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new SolRing());

        putSchemeAbilityOnStack();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(creature.getCard(), artifact.getCard());
    }

    @Test
    @DisplayName("Allows regeneration to replace destruction")
    void regenerationSavesCreature() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        protectedCreature.setRegenerationShield(1);
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());

        putSchemeAbilityOnStack();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(protectedCreature);
        assertThat(protectedCreature.isTapped()).isTrue();
        assertThat(protectedCreature.getRegenerationShield()).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(otherCreature.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(protectedCreature.getCard());
    }

    @Test
    @DisplayName("Destroys permanents that enter after the ability is put on the stack")
    void usesBattlefieldAtResolution() {
        putSchemeAbilityOnStack();
        Permanent lateArtifact = harness.addToBattlefieldAndReturn(player2, new SolRing());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(lateArtifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(lateArtifact.getCard());
    }

    @Test
    @DisplayName("Resolves without destroying anything when the opponent controls only lands")
    void resolvesAgainstOpponentWithOnlyLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CommandTower());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());

        putSchemeAbilityOnStack();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
    }

    private void putSchemeAbilityOnStack() {
        BeholdThePowerOfDestruction scheme = new BeholdThePowerOfDestruction();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL),
                player2.getId(),
                (Zone) null));
    }
}
