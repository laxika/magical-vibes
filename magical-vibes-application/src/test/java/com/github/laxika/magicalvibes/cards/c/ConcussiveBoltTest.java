package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.TezzeretAgentOfBolas;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConcussiveBolt.class, GrizzlyBears.class, LeoninScimitar.class, Spellbook.class,
        TezzeretAgentOfBolas.class})
class ConcussiveBoltTest extends BaseCardTest {


    @Test
    @DisplayName("Deals 4 damage to target player without metalcraft")
    void deals4DamageToPlayerWithoutMetalcraft() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not prevent blocking without metalcraft")
    void doesNotPreventBlockingWithoutMetalcraft() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }


    @Test
    @DisplayName("Deals 4 damage and prevents blocking with metalcraft")
    void deals4DamageAndPreventsBlockingWithMetalcraft() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);
        addThreeArtifacts(player1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(bls.canBlockAttacker(gd, creature, new Permanent(new GrizzlyBears()),
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("All creatures of target player can't block with metalcraft")
    void allCreaturesOfTargetPlayerCantBlockWithMetalcraft() {
        Permanent creature1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);
        addThreeArtifacts(player1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, creature1, new Permanent(new GrizzlyBears()),
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, creature2, new Permanent(new GrizzlyBears()),
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Metalcraft can't-block prevents declaring blockers")
    void metalcraftCantBlockPreventsDeclaringBlockers() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);
        addThreeArtifacts(player1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller's own creatures are not affected by metalcraft")
    void controllersOwnCreaturesNotAffected() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);
        addThreeArtifacts(player1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isCantBlockThisTurn()).isFalse();
    }


    @Test
    @DisplayName("Does not prevent blocking if metalcraft lost before resolution")
    void doesNotPreventBlockingIfMetalcraftLostBeforeResolution() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);
        addThreeArtifacts(player1);

        harness.castSorcery(player1, 0, player2.getId());

        // Remove artifacts before resolution
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Spellbook") || p.getCard().getName().equals("Leonin Scimitar"));

        harness.passBothPriorities();

        // Damage still dealt
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        // But blocking not prevented
        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }


    private void addThreeArtifacts(Player player) {
        harness.addToBattlefield(player, new Spellbook());
        harness.addToBattlefield(player, new LeoninScimitar());
        harness.addToBattlefield(player, new Spellbook());
    }

    @Test
    void canDamagePlaneswalkerAndRestrictItsControllersCreatures() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TezzeretAgentOfBolas());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addThreeArtifacts(player1);
        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(bls.canBlockAttacker(gd, creature, new Permanent(new GrizzlyBears()),
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void creaturesEnteringAfterResolutionCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addThreeArtifacts(player1);
        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void metalcraftGainedBeforeResolutionPreventsBlocking() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, player2.getId());
        addThreeArtifacts(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(bls.canBlockAttacker(gd, creature, new Permanent(new GrizzlyBears()),
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }
}
