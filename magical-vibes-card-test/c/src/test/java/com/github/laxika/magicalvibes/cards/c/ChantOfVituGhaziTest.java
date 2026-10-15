package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChantOfVituGhazi.class, GrizzlyBears.class, ProdigalSorcerer.class, Shock.class})
class ChantOfVituGhaziTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from creatures and gains that much life")
    void preventsCombatDamageAndGainsLife() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        castChantOfVituGhazi();

        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Prevents noncombat damage from creatures and gains that much life")
    void preventsNoncombatDamageAndGainsLife() {
        harness.setLife(player1, 20);
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        castChantOfVituGhazi();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not prevent damage from noncreature sources")
    void doesNotPreventNoncreatureDamage() {
        harness.setLife(player1, 20);
        castChantOfVituGhazi();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Convoke taps a creature and pays part of the cost")
    void castsWithConvoke() {
        harness.setLife(player1, 20);
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChantOfVituGhazi()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convokeCreature.getId()));

        assertThat(convokeCreature.isTapped()).isTrue();

        harness.passBothPriorities();
        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Two copies do not gain life twice for the same prevented damage")
    void overlappingCopiesDoNotDoubleLifeGain() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        castChantOfVituGhazi();
        castChantOfVituGhazi();

        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Overlapping shields controlled by different players do not both gain life")
    void onlyOneCasterGainsLifeForEachPreventedEvent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        castChantOfVituGhazi();
        harness.castFromHand(player2, new ChantOfVituGhazi(), "{6}{W}{W}");
        harness.passBothPriorities();

        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.handleListChoice(player1, player1.getId().toString());
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    void affectedPlayerCanChooseTheOpponentsOverlappingShield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        castChantOfVituGhazi();
        harness.castFromHand(player2, new ChantOfVituGhazi(), "{6}{W}{W}");
        harness.passBothPriorities();
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.handleListChoice(player1, player2.getId().toString());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Prevents ability damage when its creature source has left the battlefield")
    void preventsDamageFromDepartedCreatureSource() {
        harness.setLife(player1, 20);
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        castChantOfVituGhazi();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, sorcerer.getId());
        harness.assertInGraveyard(player2, "Prodigal Sorcerer");
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Prevents damage to creatures from your own creature entering after resolution")
    void preventsOwnCreatureDamageToOpponentCreature() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castChantOfVituGhazi();
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());

        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 21);
    }

    private void castChantOfVituGhazi() {
        harness.castFromHand(player1, new ChantOfVituGhazi(), "{6}{W}{W}");
        harness.passBothPriorities();
    }
}
