package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObscuringHaze.class, EdgarMarkov.class, GrizzlyBears.class, ProdigalSorcerer.class, Shock.class})
class ObscuringHazeTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for free with a commander and prevents opponents' creature damage")
    void freeCastPreventsOpponentsCreatureDamage() {
        addCommanderToBattlefield(player1);
        harness.setLife(player1, 20);
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());

        harness.setHand(player1, List.of(new ObscuringHaze()));
        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        resolveCombat(player2);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer), null,
                player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not prevent damage from your creatures")
    void doesNotPreventDamageFromYourCreatures() {
        addCommanderToBattlefield(player1);
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ObscuringHaze()));
        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Cannot use the free alternate cost without controlling a commander")
    void freeCastRequiresCommander() {
        harness.setHand(player1, List.of(new ObscuringHaze()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can use the free alternate cost while controlling an opponent's commander")
    void freeCastWithOpponentsCommander() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player2.getId(), commander);
        commander.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, commander);
        harness.setHand(player1, List.of(new ObscuringHaze()));

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Obscuring Haze");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A commander in the command zone does not enable the free alternate cost")
    void commanderInCommandZoneDoesNotEnableFreeCast() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        harness.setHand(player1, List.of(new ObscuringHaze()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can pay the normal mana cost without controlling a commander")
    void normalCastWithoutCommanderPreventsDamage() {
        addAttacker(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new ObscuringHaze(), "{2}{G}");
        harness.passBothPriorities();
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Obscuring Haze");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Prevents damage to creatures from opponents' creatures entering after resolution")
    void preventsDamageToCreaturesFromLaterSources() {
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new ObscuringHaze(), "{2}{G}");
        harness.passBothPriorities();
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer),
                null, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(victim);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage from your creatures")
    void doesNotPreventYourNoncombatDamage() {
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new ObscuringHaze(), "{2}{G}");
        harness.passBothPriorities();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sorcerer),
                null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not prevent damage from an opponent's instant")
    void doesNotPreventInstantDamage() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new ObscuringHaze(), "{2}{G}");
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prevents damage from an opponent's creature that dies before its ability resolves")
    void preventsDamageUsingLastKnownCreatureInformation() {
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new ObscuringHaze(), "{2}{G}");
        harness.passBothPriorities();
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer),
                null, player1.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, sorcerer.getId());
        harness.assertInGraveyard(player2, "Prodigal Sorcerer");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage prevention expires when the turn ends")
    void preventionExpiresAtEndOfTurn() {
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new ObscuringHaze(), "{2}{G}");
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer),
                null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    private void addCommanderToBattlefield(Player player) {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player.getId(), commander);
        harness.addToBattlefield(player, commander);
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.equals(player1) ? player2.getId() : player1.getId());
        return attacker;
    }
}
