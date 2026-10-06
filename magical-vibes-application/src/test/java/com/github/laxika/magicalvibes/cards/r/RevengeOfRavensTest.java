package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.t.TuinvaleTreefolk;
import com.github.laxika.magicalvibes.cards.g.GarrukCursedHuntsman;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RevengeOfRavens.class, TuinvaleTreefolk.class, GarrukCursedHuntsman.class, InvasionOfZendikar.class})
class RevengeOfRavensTest extends BaseCardTest {

    private void setUpAttack(Permanent... attackers) {
        harness.addToBattlefield(player1, new RevengeOfRavens());

        for (Permanent attacker : attackers) {
            attacker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(attacker);
        }
    }

    @Test
    @DisplayName("Attacking causes life loss and life gain")
    void attackerControllerLosesLifeAndRevengeControllerGainsLife() {
        setUpAttack(new Permanent(new TuinvaleTreefolk()));

        declareAttackers(player2, List.of(0));
        resolveTopTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The trigger resolves once for each attacking creature")
    void triggersOncePerAttackingCreature() {
        setUpAttack(new Permanent(new TuinvaleTreefolk()), new Permanent(new TuinvaleTreefolk()));

        declareAttackers(player2, List.of(0, 1));
        resolveTopTrigger();
        resolveTopTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declaring no attackers does not trigger Revenge of Ravens")
    void noAttackersNoTrigger() {
        setUpAttack(new Permanent(new TuinvaleTreefolk()));

        declareAttackers(player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Attacking a planeswalker also causes life loss and life gain")
    void attackingPlaneswalkerTriggers() {
        setUpAttack(new Permanent(new TuinvaleTreefolk()));
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player1, new GarrukCursedHuntsman());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));
        resolveTopTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The attacker leaving the battlefield does not stop the life loss")
    void removedAttackerStillCausesLifeLoss() {
        Permanent attacker = new Permanent(new TuinvaleTreefolk());
        setUpAttack(attacker);
        declareAttackers(player2, List.of(0));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, attacker));
        resolveTopTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Removing Revenge of Ravens does not stop its pending trigger")
    void removedEnchantmentStillGainsLife() {
        setUpAttack(new Permanent(new TuinvaleTreefolk()));
        Permanent revenge = gd.playerBattlefields.get(player1.getId()).getFirst();
        declareAttackers(player2, List.of(0));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, revenge));
        resolveTopTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Each copy triggers separately for an attacker")
    void multipleCopiesTriggerSeparately() {
        setUpAttack(new Permanent(new TuinvaleTreefolk()));
        harness.addToBattlefield(player1, new RevengeOfRavens());

        declareAttackers(player2, List.of(0));
        resolveTopTrigger();
        resolveTopTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Attacking a battle does not trigger Revenge of Ravens")
    void attackingBattleDoesNotTrigger() {
        setUpAttack(new Permanent(new TuinvaleTreefolk()));
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player1.getId());
        harness.addToBattlefield(player2, new RevengeOfRavens());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, battle.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void resolveTopTrigger() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
