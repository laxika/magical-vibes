package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObscuringHaze.class, EdgarMarkov.class, GrizzlyBears.class, ProdigalSorcerer.class, Shock.class})
class ObscuringHazeTest extends BaseCardTest {

    @Test
    void preventsCombatAndNoncombatDamageFromOpponentsCreatures() {
        harness.setLife(player1, 20);
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());

        castObscuringHaze();

        resolveCombat(player2);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer), null,
                player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
    }

    @Test
    void doesNotPreventDamageFromYourCreatures() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1, new GrizzlyBears());

        castObscuringHaze();
        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    void doesNotPreventDamageFromNoncreatureSources() {
        harness.setLife(player1, 20);
        castObscuringHaze();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void commanderAllowsCastingWithoutPayingManaCost() {
        addToCommandZone(player1, new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ObscuringHaze()));

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotUseFreeCastWithoutControllingRegisteredCommander() {
        addToCommandZone(player1, new EdgarMarkov());
        harness.setHand(player1, List.of(new ObscuringHaze()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.equals(player1) ? player2.getId() : player1.getId());
        return attacker;
    }

    private void addToCommandZone(Player player, Card card) {
        gd.playerCommandZones.get(player.getId()).add(card);
    }

    private void castObscuringHaze() {
        harness.setHand(player1, List.of(new ObscuringHaze()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
