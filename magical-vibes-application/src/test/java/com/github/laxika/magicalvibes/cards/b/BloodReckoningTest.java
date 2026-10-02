package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GarrukPrimalHunter;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodReckoning.class, WalkingCorpse.class, GarrukPrimalHunter.class, Unsummon.class})
class BloodReckoningTest extends BaseCardTest {

    /** Puts Blood Reckoning on player1's battlefield and the given attackers on player2's. */
    private void setUpAttack(Permanent... attackers) {
        harness.addToBattlefield(player1, new BloodReckoning());

        for (Permanent attacker : attackers) {
            attacker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(attacker);
        }
    }

    @Test
    @DisplayName("An attacking creature's controller loses 1 life when the trigger resolves")
    void attackerControllerLosesOneLife() {
        setUpAttack(new Permanent(new WalkingCorpse()));
        int startingLife = gd.getLife(player2.getId());

        declareAttackers(player2, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The trigger fires once per attacking creature")
    void triggersOncePerAttacker() {
        setUpAttack(new Permanent(new WalkingCorpse()), new Permanent(new WalkingCorpse()));
        int startingLife = gd.getLife(player2.getId());

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        harness.inMutationScope(() -> {
            harness.getStackResolutionService().resolveTopOfStack(gd);
            harness.getStackResolutionService().resolveTopOfStack(gd);
        });

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
    }

    @Test
    @DisplayName("Declaring no attackers produces no trigger and no life loss")
    void noAttackersNoTrigger() {
        setUpAttack(new Permanent(new WalkingCorpse()));

        declareAttackers(player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Attacking a planeswalker controlled by the enchantment's controller causes life loss")
    void attackingPlaneswalkerCausesLifeLoss() {
        setUpAttack(new Permanent(new WalkingCorpse()));
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new GarrukPrimalHunter());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each Blood Reckoning triggers independently for the same attacker")
    void multipleEnchantmentsCauseMultipleLifeLoss() {
        setUpAttack(new Permanent(new WalkingCorpse()));
        harness.addToBattlefield(player1, new BloodReckoning());

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(2);
        harness.inMutationScope(() -> {
            harness.getStackResolutionService().resolveTopOfStack(gd);
            harness.getStackResolutionService().resolveTopOfStack(gd);
        });

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The attacker controller still loses life if the attacker is returned to hand in response")
    void attackerLeavingBattlefieldDoesNotPreventLifeLoss() {
        Permanent attacker = new Permanent(new WalkingCorpse());
        setUpAttack(attacker);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player2, List.of(0));
        harness.castInstant(player1, 0, attacker.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.assertInHand(player2, "Walking Corpse");
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Creatures attacking the opponent do not trigger their controller's Blood Reckoning")
    void ownAttackerDoesNotTriggerEnchantment() {
        harness.addToBattlefield(player1, new BloodReckoning());
        addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Removing Blood Reckoning after it triggers does not stop the life loss")
    void removingEnchantmentDoesNotStopTrigger() {
        setUpAttack(new Permanent(new WalkingCorpse()));
        Permanent enchantment = gd.playerBattlefields.get(player1.getId()).getFirst();

        declareAttackers(player2, List.of(0));
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchantment));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }
}
