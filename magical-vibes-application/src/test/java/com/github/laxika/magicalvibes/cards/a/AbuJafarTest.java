package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Abu Ja'far")
class AbuJafarTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies attacking, it destroys every creature blocking it")
    void destroysAllBlockersWhenItDiesAttacking() {
        Permanent abu = addCreatureReady(player1, new AbuJafar());
        Permanent blockerOne = addCreatureReady(player2, new GrizzlyBears());
        blockerOne.setRegenerationShield(1);
        Permanent blockerTwo = addCreatureReady(player2, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(abu)));
        prepareDeclareBlockers(player1);
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

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(abu),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Abu Ja'far");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bystander);
    }
}
