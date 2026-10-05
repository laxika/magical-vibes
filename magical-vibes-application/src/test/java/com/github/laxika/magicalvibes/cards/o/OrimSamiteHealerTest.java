package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CloudchaserEagle;
import com.github.laxika.magicalvibes.cards.l.LightningBlast;
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

@CardUsed({OrimSamiteHealer.class, CloudchaserEagle.class, LightningBlast.class})
class OrimSamiteHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability taps Orim and sets a 3-damage prevention shield on resolution")
    void activationSetsShield() {
        Permanent orim = addCreatureReady(player1, new OrimSamiteHealer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player1.getId());
        assertThat(orim.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shield prevents combat damage to a player and keeps the unused remainder")
    void preventsCombatDamageToPlayer() {
        addCreatureReady(player2, new OrimSamiteHealer());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player1, new CloudchaserEagle());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Shield prevents noncombat damage to its target")
    void preventsNoncombatDamageToTarget() {
        addCreatureReady(player1, new OrimSamiteHealer());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBlast()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Shield saves a creature from lethal combat damage")
    void preventsCombatDamageToCreature() {
        addCreatureReady(player1, new OrimSamiteHealer());
        Permanent blocker = addCreatureReady(player2, new CloudchaserEagle());
        Permanent attacker = addCreatureReady(player1, new CloudchaserEagle());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        declareAttackers(player1, List.of(attackerIndex));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Unused prevention shield wears off at end of turn")
    void shieldClearedAtEndOfTurn() {
        addCreatureReady(player1, new OrimSamiteHealer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Orim can protect itself, but the shield is exhausted by the first damage event")
    void selfProtectionIsConsumed() {
        Permanent orim = addCreatureReady(player1, new OrimSamiteHealer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, orim.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningBlast(), new LightningBlast()));
        harness.addMana(player2, ManaColor.RED, 8);
        harness.castAndResolveInstant(player2, 0, orim.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(orim);
        assertThat(orim.getDamagePreventionShield()).isZero();

        harness.castAndResolveInstant(player2, 0, orim.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(orim);
        harness.assertInGraveyard(player1, "Orim, Samite Healer");
    }

    @Test
    @DisplayName("The activated ability resolves even if Orim is destroyed in response")
    void abilitySurvivesSourceRemoval() {
        Permanent orim = addCreatureReady(player1, new OrimSamiteHealer());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.setHand(player2, List.of(new LightningBlast(), new LightningBlast()));
        harness.addMana(player2, ManaColor.RED, 8);
        harness.castAndResolveInstant(player2, 0, orim.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(orim);

        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 19);
    }
}
