package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlloyMyr;
import com.github.laxika.magicalvibes.cards.o.OgreMenial;
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

@CardUsed({RuthlessInvasion.class, OgreMenial.class, AlloyMyr.class})
class RuthlessInvasionTest extends BaseCardTest {

    @Test
    @DisplayName("Nonartifact creatures can't block this turn after resolution")
    void nonartifactCreaturesCantBlock() {
        Permanent bears = addCreatureReady(player2, new OgreMenial());

        harness.setHand(player1, List.of(new RuthlessInvasion()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent attackerForPlayer1 = addCreatureReady(player1, new OgreMenial());

        assertThat(bls.canBlockAttacker(gd, bears, attackerForPlayer1,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Artifact creatures are NOT affected")
    void artifactCreaturesNotAffected() {
        Permanent myr = addCreatureReady(player2, new AlloyMyr());

        harness.setHand(player1, List.of(new RuthlessInvasion()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent attackerForPlayer1 = addCreatureReady(player1, new OgreMenial());

        assertThat(bls.canBlockAttacker(gd, myr, attackerForPlayer1,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Affects both players' nonartifact creatures")
    void affectsBothPlayersNonartifactCreatures() {
        Permanent ownBears = addCreatureReady(player1, new OgreMenial());
        Permanent oppBears = addCreatureReady(player2, new OgreMenial());
        Permanent oppMyr = addCreatureReady(player2, new AlloyMyr());

        harness.setHand(player1, List.of(new RuthlessInvasion()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, ownBears, oppBears,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, oppBears, ownBears,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, oppMyr, ownBears,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Nonartifact creature cannot declare as blocker after resolution")
    void cantBlockPreventsDeclaringBlockers() {
        Permanent attacker = addCreatureReady(player1, new OgreMenial());
        addCreatureReady(player2, new OgreMenial());

        harness.setHand(player1, List.of(new RuthlessInvasion()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact creature CAN still block after resolution")
    void artifactCreatureCanStillBlock() {
        Permanent attacker = addCreatureReady(player1, new OgreMenial());
        Permanent blocker = addCreatureReady(player2, new AlloyMyr());

        harness.setHand(player1, List.of(new RuthlessInvasion()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Nonartifact creatures entering after resolution also cannot block")
    void affectsCreaturesEnteringLater() {
        Permanent attacker = addCreatureReady(player1, new OgreMenial());
        harness.setHand(player1, List.of(new RuthlessInvasion()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        Permanent lateNonartifact = addCreatureReady(player2, new OgreMenial());
        Permanent lateArtifact = addCreatureReady(player2, new AlloyMyr());

        assertThat(bls.canBlockAttacker(gd, lateNonartifact, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, lateArtifact, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires after the turn")
    void restrictionExpiresAfterTurn() {
        Permanent attacker = addCreatureReady(player1, new OgreMenial());
        Permanent blocker = addCreatureReady(player2, new OgreMenial());
        harness.setHand(player1, List.of(new RuthlessInvasion()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("The Phyrexian symbol can be paid with two life and three generic mana")
    void canPayPhyrexianCostWithLife() {
        Permanent attacker = addCreatureReady(player1, new OgreMenial());
        Permanent blocker = addCreatureReady(player2, new OgreMenial());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new RuthlessInvasion()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Ruthless Invasion");
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }
}
