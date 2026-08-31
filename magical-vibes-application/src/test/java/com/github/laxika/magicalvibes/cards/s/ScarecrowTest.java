package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GhostShip;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Scarecrow.class, GhostShip.class, ScarwoodGoblins.class})
class ScarecrowTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from flying creatures but not nonflying creatures")
    void preventsDamageFromFlyingCreaturesOnly() {
        harness.setLife(player1, 20);
        Permanent scarecrow = addCreatureReady(player1, new Scarecrow());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        assertThat(scarecrow.isTapped()).isTrue();
        harness.passBothPriorities();

        addAttacker(player2, new GhostShip());
        addAttacker(player2, new ScarwoodGoblins());
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not prevent damage before the ability resolves")
    void doesNotPreventDamageBeforeActivation() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Scarecrow());
        addAttacker(player2, new GhostShip());

        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void preventsDamageOnlyToItsController() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new Scarecrow());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        addAttacker(player1, new GhostShip(), player2);
        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void preventionExpiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new Scarecrow());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        addAttacker(player2, new GhostShip());
        resolveCombat(player2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        addAttacker(player2, new GhostShip());
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    private Permanent addAttacker(Player owner, Card card) {
        return addAttacker(owner, card, player1);
    }

    private Permanent addAttacker(Player owner, Card card, Player target) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(target.getId());
        return attacker;
    }
}
