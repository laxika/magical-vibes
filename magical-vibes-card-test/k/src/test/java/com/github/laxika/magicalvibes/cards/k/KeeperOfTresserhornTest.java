package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SoldeviSentry;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({KeeperOfTresserhorn.class, SoldeviSentry.class, LeylineOfPunishment.class, Unsummon.class})
class KeeperOfTresserhornTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent atk = addCreatureReady(player1, new KeeperOfTresserhorn());
        atk.setAttacking(true);
        return atk;
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, assignments);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Unblocked: defending player loses 2 life and the attacker deals no combat damage")
    void unblockedDrainsTwoAndDealsNoDamage() {
        Permanent attacker = addAttacker();
        int startingLife = gd.getLife(player2.getId());

        declareBlockers(List.of());

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        // The 6/6 assigns no combat damage, so only the 2 life loss happened.
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
    }

    @Test
    @DisplayName("Blocked: no life loss and normal combat damage is dealt")
    void blockedDoesNotTrigger() {
        Permanent attacker = addAttacker();
        Permanent blocker = addCreatureReady(player2, new SoldeviSentry());
        int startingLife = gd.getLife(player2.getId());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        declareBlockers(List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Assigning no combat damage still applies when damage cannot be prevented")
    void assignsNoCombatDamageWithLeylineOfPunishment() {
        addAttacker();
        harness.addToBattlefield(player2, new LeylineOfPunishment());
        int startingLife = gd.getLife(player2.getId());

        declareBlockers(List.of());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, startingLife - 2);
    }

    @Test
    @DisplayName("Life loss resolves even if Keeper leaves the battlefield in response")
    void lifeLossResolvesAfterSourceIsReturnedToHand() {
        Permanent attacker = addAttacker();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        int startingLife = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player2, 0, attacker.getId());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInHand(player1, "Keeper of Tresserhorn");
        harness.assertNotOnBattlefield(player1, "Keeper of Tresserhorn");
        harness.assertLife(player2, startingLife - 2);
    }

    @Test
    @DisplayName("Each unblocked Keeper causes its own life loss")
    void multipleUnblockedKeepersEachCauseLifeLoss() {
        addAttacker();
        addAttacker();
        int startingLife = gd.getLife(player2.getId());

        declareBlockers(List.of());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, startingLife - 4);
    }
}
