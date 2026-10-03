package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BeetleformMage;
import com.github.laxika.magicalvibes.cards.b.BorosMastiff;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AweForTheGuilds.class, BorosMastiff.class, BeetleformMage.class, Ornithopter.class})
class AweForTheGuildsTest extends BaseCardTest {

    @Test
    @DisplayName("Monocolored creatures can't block this turn")
    void monocoloredCantBlock() {
        Permanent mastiff = addCreatureReady(player2, new BorosMastiff());

        castAweForTheGuilds();

        Permanent attackerForPlayer1 = addCreatureReady(player1, new BorosMastiff());

        assertThat(bls.canBlockAttacker(gd, mastiff, attackerForPlayer1,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Multicolored and colorless creatures are unaffected")
    void othersUnaffected() {
        Permanent mage = addCreatureReady(player2, new BeetleformMage());
        Permanent thopter = addCreatureReady(player2, new Ornithopter());

        castAweForTheGuilds();

        Permanent attackerForPlayer1 = addCreatureReady(player1, new BorosMastiff());

        assertThat(bls.canBlockAttacker(gd, mage, attackerForPlayer1,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, thopter, attackerForPlayer1,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Applies to both players' monocolored creatures")
    void affectsAllPlayers() {
        Permanent ownMastiff = addCreatureReady(player1, new BorosMastiff());
        Permanent opponentMastiff = addCreatureReady(player2, new BorosMastiff());

        castAweForTheGuilds();

        assertThat(bls.canBlockAttacker(gd, ownMastiff, opponentMastiff,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, opponentMastiff, ownMastiff,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("A restricted monocolored creature can't be declared as a blocker")
    void restrictedCreatureCantBeDeclaredBlocker() {
        Permanent attacker = addCreatureReady(player1, new BorosMastiff());
        addCreatureReady(player2, new BorosMastiff());

        castAweForTheGuilds();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A multicolored creature can still be declared as a blocker")
    void multicoloredCanStillBlock() {
        Permanent attacker = addCreatureReady(player1, new BorosMastiff());
        Permanent mage = addCreatureReady(player2, new BeetleformMage());

        castAweForTheGuilds();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(mage.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Monocolored creatures entering after resolution also can't block")
    void creaturesEnteringLaterCantBlock() {
        castAweForTheGuilds();

        Permanent attacker = addCreatureReady(player1, new BorosMastiff());
        Permanent blocker = addCreatureReady(player2, new BorosMastiff());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("The blocking restriction expires at the end of the turn")
    void restrictionExpires() {
        Permanent attacker = addCreatureReady(player1, new BorosMastiff());
        Permanent blocker = addCreatureReady(player2, new BorosMastiff());

        castAweForTheGuilds();

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    private void castAweForTheGuilds() {
        harness.setHand(player1, List.of(new AweForTheGuilds()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }
}
