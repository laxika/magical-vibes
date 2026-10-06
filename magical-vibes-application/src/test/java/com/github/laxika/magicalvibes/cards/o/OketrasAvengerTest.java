package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
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

@CardUsed({OketrasAvenger.class, GrizzlyBears.class, ProdigalSorcerer.class, LayClaim.class})
class OketrasAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Exerting prevents all combat damage that would be dealt to Oketra's Avenger")
    void exertPreventsCombatDamageToAvenger() {
        // 3/1 attacks and exerts; a 2/2 blocker's 2 combat damage would be lethal but is prevented.
        Permanent avenger = addCreatureReady(player1, new OketrasAvenger());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        blockAndResolveCombat(0, 0);

        // Combat damage to the exerted attacker is prevented, so it survives; the 2/2 still takes 3 and dies.
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(avenger);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Declining exert leaves Oketra's Avenger to die to lethal combat damage")
    void decliningExertLeavesAvengerVulnerable() {
        Permanent avenger = addCreatureReady(player1, new OketrasAvenger());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);

        blockAndResolveCombat(0, 0);

        // No prevention: the 2/2 deals 2 to the 1-toughness attacker, killing it.
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(avenger);
    }

    @Test
    @DisplayName("Exerting only prevents combat damage — noncombat damage still kills Oketra's Avenger")
    void exertDoesNotPreventNoncombatDamage() {
        Permanent avenger = addCreatureReady(player1, new OketrasAvenger());
        addCreatureReady(player1, new ProdigalSorcerer()); // T: deal 1 damage to any target

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        // Ping the exerted attacker for 1 noncombat damage — combat-only prevention does not stop it.
        harness.activateAbility(player1, 1, null, avenger.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(avenger);
    }

    @Test
    @DisplayName("Exerting keeps Oketra's Avenger tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent avenger = addCreatureReady(player1, new OketrasAvenger());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(avenger.isTapped()).isTrue();
        assertThat(avenger.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Exert is paid before the prevention trigger resolves")
    void exertIsPaidBeforePreventionResolves() {
        Permanent avenger = addCreatureReady(player1, new OketrasAvenger());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);

            assertThat(avenger.getSkipUntapCount()).isPositive();
            assertThat(gd.creaturesWithCombatDamagePrevented).doesNotContain(avenger.getId());
        });
    }

    @Test
    @DisplayName("Exert skips exactly the next untap step of the player who exerted")
    void exertSkipsOnlyOneUntapStep() {
        Permanent avenger = addCreatureReady(player1, new OketrasAvenger());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(avenger.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(avenger.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(avenger.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exert does not prevent untapping during a new controller's untap step")
    void exertRestrictionDoesNotFollowNewController() {
        Permanent avenger = addCreatureReady(player1, new OketrasAvenger());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LayClaim()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.ensurePriority(player2);
        harness.castEnchantment(player2, 0, avenger.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(avenger);
        harness.performUntapStep(player2);
        assertThat(avenger.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Combat prevention expires at end of turn")
    void preventionExpiresAtEndOfTurn() {
        Permanent avenger = addCreatureReady(player1, new OketrasAvenger());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new OketrasAvenger()));

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);
        avenger.untap();

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(avenger);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
    }

    private void blockAndResolveCombat(int blockerIndex, int attackerIndex) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();
    }
}
