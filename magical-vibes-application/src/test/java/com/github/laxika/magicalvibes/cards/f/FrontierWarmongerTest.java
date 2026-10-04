package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraLegacyOfFire;
import com.github.laxika.magicalvibes.cards.p.PilgrimsEye;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrontierWarmonger.class, PilgrimsEye.class, ChandraLegacyOfFire.class})
class FrontierWarmongerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control gain menace")
    void grantsMenaceToAttackers() {
        addCreatureReady(player1, new FrontierWarmonger());
        Permanent attacker = addCreatureReady(player1, new PilgrimsEye());
        Permanent nonattacker = addCreatureReady(player1, new PilgrimsEye());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Menace granted by Frontier Warmonger lasts only until end of turn")
    void menaceWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new FrontierWarmonger());
        Permanent attacker = addCreatureReady(player1, new PilgrimsEye());

        declareAttackers(List.of(1));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Frontier Warmonger gains menace when it attacks")
    void grantsMenaceToItself() {
        Permanent warmonger = addCreatureReady(player1, new FrontierWarmonger());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, warmonger, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("All creatures in the declared attacking group gain menace")
    void grantsMenaceToEveryDeclaredAttacker() {
        addCreatureReady(player1, new FrontierWarmonger());
        Permanent first = addCreatureReady(player1, new PilgrimsEye());
        Permanent second = addCreatureReady(player1, new PilgrimsEye());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, first, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Creatures attacking an opponent's planeswalker gain menace")
    void attackingOpponentsPlaneswalkerGrantsMenace() {
        addCreatureReady(player1, new FrontierWarmonger());
        Permanent attacker = addCreatureReady(player1, new PilgrimsEye());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraLegacyOfFire());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(1), Map.of(1, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("A declared attacker still gains menace if removed from combat before resolution")
    void removedAttackerStillGainsMenace() {
        addCreatureReady(player1, new FrontierWarmonger());
        Permanent attacker = addCreatureReady(player1, new PilgrimsEye());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        // Model removal from combat while the attack trigger is pending.
        attacker.setAttacking(false);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("A creature put onto the battlefield attacking is not part of the declared group")
    void newlyAttackingCreatureDoesNotGainMenace() {
        addCreatureReady(player1, new FrontierWarmonger());
        Permanent declaredAttacker = addCreatureReady(player1, new PilgrimsEye());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        Permanent laterAttacker = addCreatureReady(player1, new PilgrimsEye());
        laterAttacker.setAttacking(true);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, declaredAttacker, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterAttacker, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Creatures attacking Warmonger's controller do not gain menace")
    void attackingWarmongersControllerDoesNotGrantMenace() {
        addCreatureReady(player1, new FrontierWarmonger());
        Permanent attacker = addCreatureReady(player2, new PilgrimsEye());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isFalse();
    }
}
