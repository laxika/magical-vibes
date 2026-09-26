package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.cards.p.PsionicEntity;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LadyEvangela.class, BarbaryApes.class, Karakas.class, PsionicEntity.class})
class LadyEvangelaTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from the target creature and taps Lady Evangela")
    void preventsCombatDamageFromTargetCreature() {
        harness.setLife(player1, 20);
        Permanent evangela = addCreatureReady(player1, new LadyEvangela());
        Permanent attacker = addAttacker(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(evangela.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevents combat damage dealt by the target creature to a blocker")
    void preventsCombatDamageToBlocker() {
        Permanent evangela = addCreatureReady(player1, new LadyEvangela());
        Permanent blocker = addCreatureReady(player1, new BarbaryApes());
        Permanent attacker = addCreatureReady(player2, new BarbaryApes());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker), 0)));

        addActivationMana();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(evangela), null, attacker.getId());
        harness.passBothPriorities();
        resolveCombat(player2);

        harness.assertInGraveyard(player2, "Barbary Apes");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents combat damage only, not noncombat damage from the target creature")
    void preventsCombatDamageOnly() {
        addCreatureReady(player1, new LadyEvangela());
        addCreatureReady(player1, new PsionicEntity());
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, 0, null,
                gd.playerBattlefields.get(player1.getId()).get(1).getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Psionic Entity");
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        addCreatureReady(player1, new LadyEvangela());
        Permanent attacker = addAttacker(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new LadyEvangela());
        Permanent karakas = harness.addToBattlefieldAndReturn(player2, new Karakas());
        addActivationMana();

        UUID targetId = karakas.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new BarbaryApes());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
