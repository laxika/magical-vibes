package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CavernHoardDragon.class, Spellbook.class, LeoninScimitar.class,
        Ornithopter.class, GrizzlyBears.class})
class CavernHoardDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Costs less by the greatest number of artifacts controlled by an opponent")
    void costsLessByGreatestOpponentArtifactCount() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new Spellbook());
        }
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new CavernHoardDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not reduce its cost for artifacts controlled by its controller")
    void doesNotCountControllerArtifactsForCostReduction() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new CavernHoardDragon()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Creates a Treasure for each artifact controlled by the damaged player")
    void createsTreasureForEachArtifactControlledByDamagedPlayer() {
        Permanent dragon = addCreatureReady(player1, new CavernHoardDragon());
        dragon.setAttacking(true);

        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        resolveDragonCombat();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Does not create Treasures when blocked")
    void doesNotCreateTreasureWhenBlocked() {
        addCreatureReady(player1, new CavernHoardDragon());
        addCreatureReady(player2, new CavernHoardDragon());
        harness.addToBattlefield(player2, new Spellbook());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void resolveDragonCombat() {
        resolveCombat();
        resolveAllTriggers();
    }

    @Test
    void controllerArtifactsCannotMakeTheSpellAffordable() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new CavernHoardDragon()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void costReductionCanRemoveAllGenericManaButNotRedMana() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player2, new Spellbook());
        }
        harness.setHand(player1, List.of(new CavernHoardDragon()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cavern-Hoard Dragon")).hasSize(1);
    }

    @Test
    void createsNoTreasureWhenDamagedPlayerControlsNoArtifacts() {
        Permanent dragon = addCreatureReady(player1, new CavernHoardDragon());
        dragon.setAttacking(true);
        harness.addToBattlefield(player1, new Spellbook());

        resolveDragonCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void trampleTriggersAndDoesNotCountArtifactBlockerThatDied() {
        addCreatureReady(player1, new CavernHoardDragon());
        Permanent blocker = addCreatureReady(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Spellbook());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        gs.handleCombatDamageAssigned(gd, player1, 0, java.util.Map.of(
                blocker.getId(), 2, player2.getId(), 4));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(findPermanents(player2, "Ornithopter")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void countsArtifactsAtResolutionAndResolvesAfterDragonLeavesBattlefield() {
        Permanent dragon = addCreatureReady(player1, new CavernHoardDragon());
        dragon.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerBattlefields.get(player1.getId()).remove(dragon);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }
}
