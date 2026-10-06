package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GhostShip;
import com.github.laxika.magicalvibes.cards.f.Fissure;
import com.github.laxika.magicalvibes.cards.j.Jump;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scarecrow.class, GhostShip.class, ScarwoodGoblins.class, Jump.class, ProdigalSorcerer.class, Fissure.class})
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

    @Test
    void preventsAllDamageFromMultipleFlyingCreatures() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new Scarecrow());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        addAttacker(player2, new GhostShip());
        addAttacker(player2, new GhostShip());
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotPreventDamageToControlledCreatures() {
        addCreatureReady(player1, new Scarecrow());
        Permanent blocker = addCreatureReady(player1, new GhostShip());
        addCreatureReady(player2, new GhostShip());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveCombat(player2);

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotActivateWithOnlyFiveMana() {
        Permanent scarecrow = addCreatureReady(player1, new Scarecrow());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(scarecrow.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new Scarecrow());
        scarecrow.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(scarecrow.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void preventsNoncombatDamageFromCreatureThatGainsFlyingAfterResolution() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new Scarecrow());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Jump()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, sorcerer.getId());
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void preventsDamageUsingLastKnownFlyingWhenSourceIsDestroyed() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new Scarecrow());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Jump()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, sorcerer.getId());
        harness.activateAbility(player2, 0, null, player1.getId());

        harness.setHand(player1, List.of(new Fissure()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveInstant(player1, 0, sorcerer.getId());
        harness.assertInGraveyard(player2, "Prodigal Sorcerer");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
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
