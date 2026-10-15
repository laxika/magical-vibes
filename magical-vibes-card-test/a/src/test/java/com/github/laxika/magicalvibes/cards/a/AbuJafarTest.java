package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Abu Ja'far")
@CardUsed({AbuJafar.class, GrizzlyBears.class})
class AbuJafarTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies attacking, it destroys every creature blocking it")
    void destroysAllBlockersWhenItDiesAttacking() {
        Permanent abu = addCreatureReady(player1, new AbuJafar());
        Permanent blockerOne = addCreatureReady(player2, new GrizzlyBears());
        blockerOne.setRegenerationShield(1);
        Permanent blockerTwo = addCreatureReady(player2, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(abu)));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerOne),
                        gd.playerBattlefields.get(player1.getId()).indexOf(abu)),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerTwo),
                        gd.playerBattlefields.get(player1.getId()).indexOf(abu))));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Abu Ja'far");
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bystander);
    }

    @Test
    @DisplayName("When it dies blocking, it destroys the creature it blocked")
    void destroysCreatureItBlockedWhenItDiesBlocking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent abu = addCreatureReady(player2, new AbuJafar());
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(abu),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Abu Ja'far");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bystander);
    }

    @Test
    @DisplayName("Dying outside combat does not destroy unrelated creatures")
    void deathOutsideCombatLeavesOtherCreaturesAlive() {
        Permanent abu = addCreatureReady(player1, new AbuJafar());
        Permanent friendly = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        abu.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Abu Ja'far");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(friendly);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposing);
    }

    @Test
    @DisplayName("Dying after regeneration does not destroy a former blocker")
    void deathAfterRegenerationLeavesFormerBlockerAlive() {
        Permanent abu = addCreatureReady(player1, new AbuJafar());
        abu.setRegenerationShield(1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(abu)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(abu))));

        abu.setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(abu);
        assertThat(abu.isAttacking()).isFalse();
        assertThat(gd.stack).isEmpty();

        abu.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Abu Ja'far");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }
}
